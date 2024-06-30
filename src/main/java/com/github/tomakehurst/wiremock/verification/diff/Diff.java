/*
 * Copyright (C) 2016-2024 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.verification.diff;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.isEmpty;
import static com.github.tomakehurst.wiremock.verification.diff.SpacerLine.SPACER;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.BinaryEqualToPattern;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.EqualToXmlPattern;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipleMatchMultiValuePattern;
import com.github.tomakehurst.wiremock.matching.PathPattern;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.SingleMatchMultiValuePattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathTemplatePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Diff {

  private final String stubMappingName;
  private final RequestPattern requestPattern;
  private final Request request;
  private final String scenarioName;
  private final String scenarioState;
  private final String expectedScenarioState;

  public Diff(RequestPattern expected, Request actual) {
    this.requestPattern = expected;
    this.request = actual;
    this.stubMappingName = null;
    this.scenarioName = null;
    this.scenarioState = null;
    this.expectedScenarioState = null;
  }

  public Diff(StubMapping expected, Request actual) {
    this(expected, actual, null);
  }

  public Diff(StubMapping expected, Request actual, String scenarioState) {
    this.requestPattern = expected.getRequest();
    this.request = actual;
    this.stubMappingName = expected.getName();
    this.scenarioName = expected.getScenarioName();
    this.scenarioState = scenarioState;
    this.expectedScenarioState = expected.getRequiredScenarioState();
  }

  @Override
  public String toString() {
    return new JUnitStyleDiffRenderer().render(this);
  }

  public List<DiffLine<?>> getLines() {
    return getLines(Collections.emptyMap());
  }

  public List<DiffLine<?>> getLines(Map<String, RequestMatcherExtension> customMatcherExtensions) {
    List<DiffLine<?>> diffLineList = new LinkedList<>();

    addHostSectionIfPresent(diffLineList);
    addPortSectionIfPresent(diffLineList);
    addSchemeSectionIfPresent(diffLineList);
    addMethodSection(diffLineList);
    UrlPattern urlPattern = getFirstNonNull(requestPattern.getUrlMatcher(), anyUrl());
    DiffLine<String> urlSection = addUrlSectionWithSpacer(urlPattern, diffLineList);

    addHeaderSectionWithSpacerIfPresent(
        requestPattern.combineBasicAuthAndOtherHeaders(), request.getHeaders(), diffLineList);

    addPathParameterSectionWithSpacerIfPresent(urlPattern, urlSection, diffLineList);

    addQueryParametersSectionWithSpacerIfPresent(diffLineList);

    addFormParametersSectionWithSpacerIfPresent(diffLineList);

    addCookiesSectionWithSpacerIfPresent(diffLineList);

    addBodySectionIfPresent(diffLineList);

    addMultipartSectionWithSpacerIfPresent(diffLineList);

    addInlineCustomMatcherSectionIfPresent(diffLineList);
    addNamedCustomMatcherSectionIfPresent(customMatcherExtensions, diffLineList);
    addScenarioSectionIfPresent(diffLineList);

    return diffLineList;
  }

  private void addScenarioSectionIfPresent(List<DiffLine<?>> diffLineList) {
    if (scenarioName != null && expectedScenarioState != null) {
      diffLineList.addAll(
          toDiffDescriptionLines(
              new DiffLine<>(
                  "Scenario",
                  new EqualToPattern(expectedScenarioState),
                  buildScenarioLine(scenarioName, scenarioState),
                  buildScenarioLine(scenarioName, expectedScenarioState))));
    }
  }

  private void addNamedCustomMatcherSectionIfPresent(
      Map<String, RequestMatcherExtension> customMatcherExtensions,
      List<DiffLine<?>> diffLineList) {
    if (requestPattern.hasNamedCustomMatcher()) {
      RequestMatcherExtension customMatcher =
          customMatcherExtensions.get(requestPattern.getCustomMatcher().getName());
      if (customMatcher != null) {
        NamedCustomMatcherLine namedCustomMatcherLine =
            new NamedCustomMatcherLine(
                customMatcher, requestPattern.getCustomMatcher().getParameters(), request);
        diffLineList.addAll(toDiffDescriptionLines(namedCustomMatcherLine));
      } else {
        diffLineList.add(
            new SectionDelimiter(
                "[custom matcher: " + requestPattern.getCustomMatcher().getName() + "]"));
      }
    }
  }

  private void addInlineCustomMatcherSectionIfPresent(List<DiffLine<?>> diffLineList) {
    if (requestPattern.hasInlineCustomMatcher()) {
      InlineCustomMatcherLine customMatcherLine =
          new InlineCustomMatcherLine(requestPattern.getMatcher(), request);
      diffLineList.addAll(toDiffDescriptionLines(customMatcherLine));
    }
  }

  private void addMultipartSectionWithSpacerIfPresent(List<DiffLine<?>> diffLineList) {
    List<MultipartValuePattern> multipartPatterns = requestPattern.getMultipartPatterns();
    if (multipartPatterns != null && !multipartPatterns.isEmpty()) {

      for (MultipartValuePattern pattern : multipartPatterns) {
        if (!request.isMultipart()) {
          diffLineList.add(new SectionDelimiter("[Multipart request body]", ""));
        } else if (!pattern.match(request).isExactMatch()) {
          for (Request.Part part : request.getParts()) {
            diffLineList.add(SPACER);
            String patternPartName = pattern.getName() == null ? "" : ": " + pattern.getName();
            String partName = part.getName() == null ? "" : part.getName();
            diffLineList.add(
                new SectionDelimiter("[Multipart" + patternPartName + "]", "[" + partName + "]"));
            diffLineList.add(SPACER);

            if (!pattern.match(part).isExactMatch()) {
              addHeaderSectionWithSpacerIfPresent(
                  pattern.getHeaders(), part.getHeaders(), diffLineList);
              addBodySectionIfPresent(pattern.getBodyPatterns(), part.getBody(), diffLineList);
              diffLineList.add(SPACER);
            }

            diffLineList.add(new SectionDelimiter("[/Multipart]", "[/" + partName + "]"));
            diffLineList.add(SPACER);
          }
        }
      }
    }
  }

  private void addCookiesSectionWithSpacerIfPresent(List<DiffLine<?>> diffLineList) {
    Map<String, StringValuePattern> cookiesPattern = requestPattern.getCookies();
    if (cookiesPattern != null) {
      Map<String, Cookie> cookies = getFirstNonNull(request.getCookies(), Collections.emptyMap());
      for (Map.Entry<String, StringValuePattern> entry : cookiesPattern.entrySet()) {
        String key = entry.getKey();
        StringValuePattern pattern = entry.getValue();
        Cookie cookie = getFirstNonNull(cookies.get(key), Cookie.absent());

        String operator = generateOperatorString(pattern, "=");
        DiffLine<String> section =
            new DiffLine<>(
                "Cookie",
                pattern,
                cookie.isPresent() ? cookie.getValue() : "",
                "Cookie: " + key + operator + pattern.getValue());
        diffLineList.addAll(toDiffDescriptionLines(section));
      }
      if (!cookiesPattern.isEmpty()) {
        diffLineList.add(SPACER);
      }
    }
  }

  private void addFormParametersSectionWithSpacerIfPresent(List<DiffLine<?>> diffLineList) {
    final Map<String, MultiValuePattern> formParameters = requestPattern.getFormParameters();
    if (formParameters != null) {
      Map<String, FormParameter> requestFormParameters = request.formParameters();

      for (Map.Entry<String, MultiValuePattern> entry : formParameters.entrySet()) {
        String key = entry.getKey();
        MultiValuePattern pattern = entry.getValue();
        FormParameter formParameter =
            getFirstNonNull(requestFormParameters.get(key), FormParameter.absent(key));

        String operator = generateOperatorStringForMultiValuePattern(pattern, " = ");
        DiffLine<MultiValue> section =
            new DiffLine<>(
                "Form data",
                pattern,
                formParameter,
                "Form: " + key + operator + pattern.getExpected());
        diffLineList.addAll(toDiffDescriptionLines(section));
      }
      if (!formParameters.isEmpty()) {
        diffLineList.add(SPACER);
      }
    }
  }

  private void addQueryParametersSectionWithSpacerIfPresent(List<DiffLine<?>> diffLineList) {
    final Map<String, MultiValuePattern> queryParameters = requestPattern.getQueryParameters();
    if (queryParameters != null) {
      Map<String, QueryParameter> requestQueryParams =
          Urls.splitQuery(URI.create(request.getUrl()));

      for (Map.Entry<String, MultiValuePattern> entry : queryParameters.entrySet()) {
        String key = entry.getKey();
        MultiValuePattern pattern = entry.getValue();
        QueryParameter queryParameter =
            getFirstNonNull(requestQueryParams.get(key), QueryParameter.absent(key));

        String operator = generateOperatorStringForMultiValuePattern(pattern, " = ");
        DiffLine<MultiValue> section =
            new DiffLine<>(
                "Query",
                pattern,
                queryParameter,
                "Query: " + key + operator + pattern.getExpected());
        diffLineList.addAll(toDiffDescriptionLines(section));
      }
      if (!queryParameters.isEmpty()) {
        diffLineList.add(SPACER);
      }
    }
  }

  private void addPathParameterSectionWithSpacerIfPresent(
      UrlPattern urlPattern, DiffLine<String> urlSection, List<DiffLine<?>> diffLineList) {
    final Map<String, StringValuePattern> pathParameters = requestPattern.getPathParameters();
    if (urlPattern instanceof UrlPathTemplatePattern
        && pathParameters != null
        && !pathParameters.isEmpty()
        && urlSection.isExactMatch()) {
      final UrlPathTemplatePattern urlPathTemplatePattern =
          (UrlPathTemplatePattern) requestPattern.getUrlMatcher();
      final PathTemplate pathTemplate = urlPathTemplatePattern.getPathTemplate();
      final PathParams requestPathParameterValues =
          pathTemplate.parse(Urls.getPath(request.getUrl()));

      for (Map.Entry<String, String> entry : requestPathParameterValues.entrySet()) {
        String parameterName = entry.getKey();
        final String parameterValue = entry.getValue();
        final StringValuePattern pattern = pathParameters.get(parameterName);
        String operator = generateOperatorString(pattern, " = ");
        DiffLine<String> section =
            new DiffLine<>(
                "Path parameter",
                pattern,
                parameterValue,
                "Path parameter: " + parameterName + operator + pattern.getValue());
        diffLineList.addAll(toDiffDescriptionLines(section));
      }

      diffLineList.add(SPACER);
    }
  }

  private DiffLine<String> addUrlSectionWithSpacer(
      UrlPattern urlPattern, List<DiffLine<?>> diffLineList) {
    String printedUrlPattern = generatePrintedUrlPattern(urlPattern);
    DiffLine<String> urlSection =
        new DiffLine<>("URL", urlPattern, request.getUrl(), printedUrlPattern);
    diffLineList.addAll(toDiffDescriptionLines(urlSection));
    diffLineList.add(SPACER);
    return urlSection;
  }

  private void addMethodSection(List<DiffLine<?>> diffLineList) {
    DiffLine<RequestMethod> methodSection =
        new DiffLine<>(
            "HTTP method",
            requestPattern.getMethod(),
            request.getMethod(),
            requestPattern.getMethod().getName());
    diffLineList.addAll(toDiffDescriptionLines(methodSection));
  }

  private void addSchemeSectionIfPresent(List<DiffLine<?>> diffLineList) {
    if (requestPattern.getScheme() != null) {
      StringValuePattern expectedScheme = equalTo(String.valueOf(requestPattern.getScheme()));
      DiffLine<String> schemeSection =
          new DiffLine<>("Scheme", expectedScheme, request.getScheme(), requestPattern.getScheme());
      diffLineList.addAll(toDiffDescriptionLines(schemeSection));
    }
  }

  private void addPortSectionIfPresent(List<DiffLine<?>> diffLineList) {
    if (requestPattern.getPort() != null) {
      StringValuePattern expectedPort = equalTo(String.valueOf(requestPattern.getPort()));
      String actualPort = String.valueOf(request.getPort());
      DiffLine<String> portSection =
          new DiffLine<>("Port", expectedPort, actualPort, expectedPort.getExpected());
      diffLineList.addAll(toDiffDescriptionLines(portSection));
    }
  }

  private void addHostSectionIfPresent(List<DiffLine<?>> diffLineList) {
    if (requestPattern.getHost() != null) {
      String hostOperator = generateOperatorString(requestPattern.getHost(), "");
      String printedHostPatternValue = hostOperator + requestPattern.getHost().getExpected();
      DiffLine<String> hostSection =
          new DiffLine<>(
              "Host", requestPattern.getHost(), request.getHost(), printedHostPatternValue.trim());
      diffLineList.addAll(toDiffDescriptionLines(hostSection));
    }
  }

  private static String buildScenarioLine(String scenarioName, String scenarioState) {
    return "[Scenario '" + scenarioName + "' state: " + scenarioState + "]";
  }

  private void addHeaderSectionWithSpacerIfPresent(
      Map<String, MultiValuePattern> headerPatterns,
      HttpHeaders headers,
      List<DiffLine<?>> diffLineList) {
    if (headerPatterns != null && !headerPatterns.isEmpty()) {
      for (String key : headerPatterns.keySet()) {
        HttpHeader header = headers.getHeader(key);
        MultiValuePattern headerPattern = headerPatterns.get(header.key());

        String operator = generateOperatorStringForMultiValuePattern(headerPattern, "");
        String expected =
            isEmpty(headerPattern.getExpected()) ? "" : ": " + headerPattern.getExpected();
        String printedPatternValue = header.key() + operator + expected;

        DiffLine<MultiValue> section =
            new DiffLine<>("Header", headerPattern, header, printedPatternValue);
        diffLineList.addAll(toDiffDescriptionLines(section));
      }
      diffLineList.add(SPACER);
    }
  }

  private void addBodySectionIfPresent(
      List<ContentPattern<?>> bodyPatterns, Body body, List<DiffLine<?>> diffLineList) {
    if (bodyPatterns != null && !bodyPatterns.isEmpty()) {
      for (ContentPattern<?> pattern : bodyPatterns) {
        String formattedBody = formatIfJsonOrXml(pattern, body);
        if (PathPattern.class.isAssignableFrom(pattern.getClass())) {
          PathPattern pathPattern = (PathPattern) pattern;
          if (!pathPattern.isSimple()) {
            String expressionResultString = getExpressionResultString(body, pathPattern);
            String printedExpectedValue =
                pathPattern.getExpected()
                    + " ["
                    + pathPattern.getValuePattern().getName()
                    + "] "
                    + pathPattern.getValuePattern().getExpected();
            if (expressionResultString != null) {
              diffLineList.addAll(
                  toDiffDescriptionLines(
                      new DiffLine<>(
                          "Body",
                          pathPattern.getValuePattern(),
                          expressionResultString,
                          printedExpectedValue)));
            } else {
              diffLineList.addAll(
                  toDiffDescriptionLines(
                      new DiffLine<>("Body", pathPattern, formattedBody, printedExpectedValue)));
            }
          } else {
            diffLineList.addAll(
                toDiffDescriptionLines(
                    new DiffLine<>("Body", pathPattern, formattedBody, pattern.getExpected())));
          }
        } else if (StringValuePattern.class.isAssignableFrom(pattern.getClass())) {
          StringValuePattern stringValuePattern = (StringValuePattern) pattern;
          String printedPatternValue = "[" + pattern.getName() + "]\n" + pattern.getExpected();
          diffLineList.addAll(
              toDiffDescriptionLines(
                  new DiffLine<>(
                      "Body", stringValuePattern, "\n" + formattedBody, printedPatternValue)));
        } else {
          BinaryEqualToPattern nonStringPattern = (BinaryEqualToPattern) pattern;
          diffLineList.addAll(
              toDiffDescriptionLines(
                  new DiffLine<>(
                      "Body", nonStringPattern, formattedBody.getBytes(), pattern.getExpected())));
        }
      }
    }
  }

  private void addBodySectionIfPresent(List<DiffLine<?>> builder) {
    List<ContentPattern<?>> bodyPatterns = requestPattern.getBodyPatterns();
    Body body = new Body(request.getBody());
    addBodySectionIfPresent(bodyPatterns, body, builder);
  }

  private static String getExpressionResultString(Body body, PathPattern pathPattern) {
    String bodyStr = body.asString();
    if (isEmpty(bodyStr)) {
      return null;
    } else {
      try {
        ListOrSingle<String> expressionResult = pathPattern.getExpressionResult(bodyStr);
        return expressionResult != null && !expressionResult.isEmpty()
            ? expressionResult.toString()
            : null;
      } catch (Exception e) {
        return null;
      }
    }
  }

  private String generatePrintedUrlPattern(UrlPattern urlPattern) {
    String matchPart;
    if (urlPattern instanceof UrlPathTemplatePattern) {
      matchPart = "path template";
    } else {
      matchPart =
          (urlPattern instanceof UrlPathPattern ? "path" : "")
              + (urlPattern.isRegex() ? " regex" : "");
    }

    matchPart = matchPart.trim();

    return matchPart.isEmpty()
        ? urlPattern.getExpected()
        : "[" + matchPart + "] " + urlPattern.getExpected();
  }

  private String generateOperatorString(ContentPattern<?> pattern, String defaultValue) {
    return isAnEqualToPattern(pattern) ? defaultValue : " [" + pattern.getName() + "] ";
  }

  private String generateOperatorStringForMultiValuePattern(
      final MultiValuePattern valuePattern, final String defaultValue) {
    if (valuePattern instanceof MultipleMatchMultiValuePattern) {
      return ((MultipleMatchMultiValuePattern) valuePattern).getOperator()
          + "["
          + valuePattern.getName()
          + "]";
    } else {
      return isAnEqualToPattern(((SingleMatchMultiValuePattern) valuePattern).getValuePattern())
          ? defaultValue
          : " [" + valuePattern.getName() + "] ";
    }
  }

  public String getStubMappingName() {
    return stubMappingName;
  }

  private static String formatIfJsonOrXml(ContentPattern<?> pattern, Body body) {
    if (body == null || body.isAbsent()) {
      return "";
    }

    try {
      return pattern.getClass().equals(EqualToJsonPattern.class)
          ? Json.prettyPrint(Json.write(body.asJson()))
          : pattern.getClass().equals(EqualToXmlPattern.class)
              ? Xml.prettyPrint(body.asString())
              : pattern.getClass().equals(BinaryEqualToPattern.class)
                  ? body.asBase64()
                  : body.asString();
    } catch (Exception e) {
      return body.asString();
    }
  }

  private static boolean isAnEqualToPattern(ContentPattern<?> pattern) {
    return pattern instanceof EqualToPattern
        || pattern instanceof EqualToJsonPattern
        || pattern instanceof EqualToXmlPattern
        || pattern instanceof BinaryEqualToPattern;
  }

  private Collection<? extends DiffLine<?>> toDiffDescriptionLines(DiffLine<?> diffLine) {
    return diffLine.getDiffDescriptions().stream()
        .map(
            diffDescription ->
                new DiffDescriptionLine<>(
                    diffDescription, diffLine.requestAttribute, diffLine.isExactMatch()))
        .collect(Collectors.toList());
  }
}
