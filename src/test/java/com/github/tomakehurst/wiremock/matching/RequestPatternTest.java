/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.not;
import static com.github.tomakehurst.wiremock.client.WireMock.notContaining;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class RequestPatternTest {

  @Test
  void matchesExactlyWith0DistanceWhenUrlAndMethodAreExactMatch() {
    RequestPattern requestPattern = newRequestPattern(PUT, urlPathEqualTo("/my/url")).build();

    MatchResult matchResult = requestPattern.match(mockRequest().method(PUT).url("/my/url"));
    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void returnsNon0DistanceWhenUrlDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url")).withUrl("/my/url").build();

    MatchResult matchResult = requestPattern.match(mockRequest().url("/totally/other/url"));
    assertThat(matchResult.getDistance(), greaterThan(0.0));
    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void matchesExactlyWith0DistanceWhenAllRequiredHeadersMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withHeader("My-Header", equalTo("my-expected-header-val"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(PUT).header("My-Header", "my-expected-header-val").url("/my/url"));
    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchWhenHeaderDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(GET, urlPathEqualTo("/my/url"))
            .withHeader("My-Header", equalTo("my-expected-header-val"))
            .withHeader("My-Other-Header", equalTo("my-other-expected-header-val"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(GET)
                .header("My-Header", "my-expected-header-val")
                .header("My-Other-Header", "wrong")
                .url("/my/url"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void matchesExactlyWhenRequiredAbsentHeaderIsAbsent() {
    RequestPattern requestPattern =
        newRequestPattern(GET, urlPathEqualTo("/my/url"))
            .withHeader("My-Header", absent())
            .withHeader("My-Other-Header", equalTo("my-other-expected-header-val"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(GET)
                .header("My-Other-Header", "my-other-expected-header-val")
                .url("/my/url"));

    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchWhenRequiredAbsentHeaderIsPresent() {
    RequestPattern requestPattern =
        newRequestPattern(GET, urlPathEqualTo("/my/url"))
            .withHeader("My-Header", absent())
            .withHeader("My-Other-Header", equalTo("my-other-expected-header-val"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(GET)
                .header("My-Header", "my-expected-header-val")
                .header("My-Other-Header", "wrong")
                .url("/my/url"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternForUrl() throws Exception {
    RequestPattern requestPattern = newRequestPattern(GET, urlEqualTo("/my/url")).build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(
        "{									                \n"
            + "		\"url\": \"/my/url\",               \n"
            + "		\"method\": \"GET\",						    \n"
            + "		\"methods\": [\"GET\"]						  \n"
            + "}												    ",
        actualJson,
        true);
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPattern() throws Exception {
    RequestPattern requestPattern = newRequestPattern(GET, urlMatching("/my/url")).build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(
        "{									                \n"
            + "		\"urlPattern\": \"/my/url\",        \n"
            + "		\"method\": \"GET\",						    \n"
            + "		\"methods\": [\"GET\"]						  \n"
            + "}												    ",
        actualJson,
        true);
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPathPattern() throws Exception {
    RequestPattern requestPattern = newRequestPattern(GET, urlPathMatching("/my/url")).build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(
        "{									                \n"
            + "		\"urlPathPattern\": \"/my/url\",    \n"
            + "		\"method\": \"GET\",						    \n"
            + "		\"methods\": [\"GET\"]						  \n"
            + "}												    ",
        actualJson,
        true);
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternForUrlPathAndHeaders() throws Exception {
    RequestPattern requestPattern =
        newRequestPattern(GET, urlPathEqualTo("/my/url"))
            .withHeader("Accept", matching("(.*)xml(.*)"))
            .withHeader("If-None-Match", matching("([a-z0-9]*)"))
            .build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(URL_PATH_AND_HEADERS_EXAMPLE, actualJson, true);
  }

  static final String URL_PATH_AND_HEADERS_EXAMPLE =
      "{									                \n"
          + "		\"urlPath\": \"/my/url\",             		\n"
          + "		\"method\": \"GET\",						\n"
          + "		\"methods\": [\"GET\"],						\n"
          + "		\"headers\": {								\n"
          + "			\"Accept\": {							\n"
          + "				\"matches\": \"(.*)xml(.*)\"		\n"
          + "			},										\n"
          + "			\"If-None-Match\": {					\n"
          + "				\"matches\": \"([a-z0-9]*)\"		\n"
          + "			}										\n"
          + "		}											\n"
          + "}												    ";

  @Test
  void matchesExactlyWith0DistanceWhenAllRequiredQueryParametersMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withQueryParam("param1", equalTo("1"))
            .withQueryParam("param2", equalTo("2"))
            .build();

    MatchResult matchResult =
        requestPattern.match(mockRequest().method(PUT).url("/my/url?param1=1&param1=555&param2=2"));
    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void returnsNon0DistanceWhenRequiredQueryParameterMatchDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withQueryParam("param1", equalTo("1"))
            .withQueryParam("param2", equalTo("2"))
            .build();

    MatchResult matchResult =
        requestPattern.match(mockRequest().method(PUT).url("/my/url?param1=555&param2=2"));
    assertThat(matchResult.getDistance(), greaterThan(0.0));
    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternWithQueryParams() throws Exception {
    RequestPattern requestPattern =
        newRequestPattern(GET, urlPathEqualTo("/my/url"))
            .withQueryParam("param1", equalTo("1"))
            .withQueryParam("param2", matching("2"))
            .build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(
        "{                         \n"
            + "    \"urlPath\": \"/my/url\",  \n"
            + "    \"method\": \"GET\",       \n"
            + "    \"methods\": [\"GET\"],    \n"
            + "    \"queryParameters\": {     \n"
            + "        \"param1\": {          \n"
            + "            \"equalTo\": \"1\" \n"
            + "        },                     \n"
            + "        \"param2\": {          \n"
            + "            \"matches\": \"2\" \n"
            + "        }                      \n"
            + "    }                          \n"
            + "}",
        actualJson,
        true);
  }

  @Test
  void matchesExactlyWith0DistanceWhenAllRequiredFormParametersMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withFormParam("key1", equalTo("value1"))
            .withFormParam("key2", equalTo("value2"))
            .build();

    Map<String, FormParameter> formParameters = new HashMap<>();
    formParameters.put("key1", new FormParameter("key1", List.of("value1")));
    formParameters.put("key2", new FormParameter("key1", List.of("value2")));
    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(PUT).url("/my/url").formParameters(formParameters));
    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void returnsNon0DistanceWhenRequiredFormParameterMatchDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withFormParam("key1", equalTo("value1"))
            .withFormParam("key2", equalTo("value2"))
            .build();

    Map<String, FormParameter> formParameters = new HashMap<>();
    formParameters.put("key1", new FormParameter("key1", List.of("value555")));
    formParameters.put("key2", new FormParameter("key1", List.of("value78")));

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(PUT).url("/my/url").formParameters(formParameters));
    assertThat(matchResult.getDistance(), greaterThan(0.0));
    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void bindsToJsonCompatibleWithOriginalRequestPatternWithFormParams() throws Exception {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withFormParam("key1", equalTo("value1"))
            .withFormParam("key2", matching("value2"))
            .build();

    String actualJson = Json.write(requestPattern);

    JSONAssert.assertEquals(getFormParameterRequestPatternJson(), actualJson, true);
  }

  @Test
  void correctlyDeserializesFormParams() {
    RequestPattern requestPattern =
        Json.read(getFormParameterRequestPatternJson(), RequestPattern.class);
    assertTrue(
        requestPattern.getFormParameters().get("key1") instanceof SingleMatchMultiValuePattern);
    assertTrue(
        requestPattern.getFormParameters().get("key2") instanceof SingleMatchMultiValuePattern);
    assertThat(
        ((SingleMatchMultiValuePattern) requestPattern.getFormParameters().get("key1"))
            .getValuePattern(),
        valuePattern(EqualToPattern.class, "value1"));
    assertThat(
        ((SingleMatchMultiValuePattern) requestPattern.getFormParameters().get("key2"))
            .getValuePattern(),
        valuePattern(RegexPattern.class, "value2"));
  }

  private String getFormParameterRequestPatternJson() {
    return "{                              \n"
        + "    \"urlPath\": \"/my/url\",  \n"
        + "    \"method\": \"POST\",       \n"
        + "    \"methods\": [\"POST\"],       \n"
        + "    \"formParameters\": {     \n"
        + "        \"key1\": {          \n"
        + "            \"equalTo\": \"value1\" \n"
        + "        },                     \n"
        + "        \"key2\": {          \n"
        + "            \"matches\": \"value2\" \n"
        + "        }                      \n"
        + "    }                          \n"
        + "}";
  }

  @Test
  void matchesExactlyWith0DistanceWhenBodyPatternsAllMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withRequestBody(equalTo("exactwordone approxwordtwo blah blah"))
            .withRequestBody(containing("two"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(PUT).url("/my/url").body("exactwordone approxwordtwo blah blah"));
    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchExactlyWhenOneBodyPatternDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withRequestBody(equalTo("exactwordone approxwordtwo blah blah"))
            .withRequestBody(containing("three"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(PUT).url("/my/url").body("exactwordone approxwordtwo blah blah"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void matchesExactlyWith0DistanceWhenMultipartPatternsAllMatch() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withAnyRequestBodyPart(
                aMultipart()
                    .withName("part-1")
                    .withHeader("Content-Type", containing("text/plain"))
                    .withBody(equalTo("body part value")))
            .withAnyRequestBodyPart(
                aMultipart()
                    .withName("part-2")
                    .withHeader("Content-Type", containing("application/octet-stream"))
                    .withBody(containing("other body")))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(POST)
                .url("/my/url")
                .header("Content-Type", "multipart/form-data; boundary=BOUNDARY")
                .multipartBody(
                    "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-1\"; filename=\"\"\r\nContent-Type: text/plain\r\n\r\n"
                        + "body part value\r\n"
                        + "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-2\"; filename=\"\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: base64\r\n\r\n"
                        + "c29tZSBvdGhlciBib2R5IHZhbHVl\r\n"
                        + // some other body value
                        "--BOUNDARY--"));

    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchExactlyWhenOneMultipartBodyPatternDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withAnyRequestBodyPart(
                aMultipart().withName("part-2").withBody(containing("non existing part")))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(PUT)
                .url("/my/url")
                .header("Content-Type", "multipart/form-data; boundary=BOUNDARY")
                .multipartBody(
                    "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-2\"; filename=\"\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: base64\r\n\r\n"
                        + "c29tZSBvdGhlciBib2R5IHZhbHVl\r\n"
                        + // some other body value
                        "--BOUNDARY--"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchExactlyWhenOneMultipartHeaderPatternDoesNotMatch() {
    RequestPattern requestPattern =
        newRequestPattern(PUT, urlPathEqualTo("/my/url"))
            .withAnyRequestBodyPart(
                aMultipart()
                    .withName("part-1")
                    .withHeader("Content-Type", containing("application/json")))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(PUT)
                .url("/my/url")
                .header("Content-Type", "multipart/form-data; boundary=BOUNDARY")
                .multipartBody(
                    "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-1\"; filename=\"\"\r\nContent-Type: application/octet-stream\r\nContent-Transfer-Encoding: base64\r\n\r\n"
                        + "c29tZSBvdGhlciBib2R5IHZhbHVl\r\n"
                        + // some other body value
                        "--BOUNDARY--"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void matchesExactlyWith0DistanceWhenAllMultipartPatternsMatchAllParts() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withAllRequestBodyParts(
                aMultipart()
                    .withHeader("Content-Type", containing("text/plain"))
                    .withBody(containing("body value")))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest()
                .method(POST)
                .url("/my/url")
                .header("Content-Type", "multipart/form-data; boundary=BOUNDARY")
                .multipartBody(
                    "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-1\"; filename=\"\"\r\nContent-Type: text/plain\r\n\r\n"
                        + "body value-1\r\n"
                        + "--BOUNDARY\r\nContent-Disposition: form-data; name=\"part-2\"; filename=\"\"\r\nContent-Type: text/plain\r\nContent-Transfer-Encoding: base64\r\n\r\n"
                        + "c29tZSBvdGhlciBib2R5IHZhbHVl\r\n"
                        + // some other body value
                        "--BOUNDARY--"));

    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void matchesExactlyWhenAllCookiesMatch() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withCookie("my_cookie", equalTo("my-cookie-value"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(POST).cookie("my_cookie", "my-cookie-value").url("/my/url"));

    assertThat(matchResult.getDistance(), is(0.0));
    assertTrue(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchWhenARequiredCookieIsMissing() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withCookie("my_cookie", equalTo("my-cookie-value"))
            .build();

    MatchResult matchResult = requestPattern.match(mockRequest().method(POST).url("/my/url"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchWhenRequiredCookieValueIsWrong() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withCookie("my_cookie", equalTo("my-cookie-value"))
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(POST).cookie("my_cookie", "wrong-value").url("/my/url"));

    assertFalse(matchResult.isExactMatch());
  }

  @Test
  void doesNotMatchWhenRequiredAbsentCookieIsPresent() {
    RequestPattern requestPattern =
        newRequestPattern(POST, urlPathEqualTo("/my/url"))
            .withCookie("my_cookie", absent())
            .build();

    MatchResult matchResult =
        requestPattern.match(
            mockRequest().method(POST).cookie("my_cookie", "any-value").url("/my/url"));

    assertFalse(matchResult.isExactMatch());
  }

  static final String ALL_BODY_PATTERNS_EXAMPLE =
      "{                                                      \n"
          + "    \"url\" : \"/all/body/patterns\",                  \n"
          + "    \"method\" : \"PUT\",                              \n"
          + "    \"methods\" : [ \"PUT\" ],                         \n"
          + "    \"bodyPatterns\" : [                               \n"
          + "        { \"equalTo\": \"thing\" },                    \n"
          + "        { \"equalToJson\": \"{ \\\"thing\\\": 1 }\" }, \n"
          + "        { \"matchesJsonPath\": \"@.*\" },              \n"
          + "        { \"equalToXml\": \"<thing />\" },             \n"
          + "        { \"matchesXPath\": \"//thing\" },             \n"
          + "        { \"contains\": \"thin\" },                    \n"
          + "        { \"doesNotContain\": \"stuff\" },            \n"
          + "        { \"not\": { \"contains\": \"thing\" } },     \n"
          + "        { \"matches\": \".*thing.*\" },                \n"
          + "        { \"doesNotMatch\": \"^stuff.+\" }             \n"
          + "    ]                                                  \n"
          + "}";

  @Test
  void correctlyDeserialisesBodyPatterns() {
    RequestPattern pattern = Json.read(ALL_BODY_PATTERNS_EXAMPLE, RequestPattern.class);
    assertThat(
        pattern.getBodyPatterns(),
        hasItems(
            valuePattern(EqualToPattern.class, "thing"),
            valuePattern(EqualToJsonPattern.class, "{ \"thing\": 1 }"),
            valuePattern(MatchesJsonPathPattern.class, "@.*"),
            valuePattern(EqualToXmlPattern.class, "<thing />"),
            valuePattern(MatchesXPathPattern.class, "//thing"),
            valuePattern(ContainsPattern.class, "thin"),
            valuePattern(NegativeContainsPattern.class, "stuff"),
            valuePattern(NotPattern.class, containing("thing").expectedValue),
            valuePattern(RegexPattern.class, ".*thing.*"),
            valuePattern(NegativeRegexPattern.class, "^stuff.+")));
  }

  @Test
  void correctlySerialisesBodyPatterns() throws Exception {
    RequestPattern requestPattern =
        newRequestPattern(RequestMethod.PUT, urlEqualTo("/all/body/patterns"))
            .withRequestBody(equalTo("thing"))
            .withRequestBody(equalToJson("{ \"thing\": 1 }"))
            .withRequestBody(matchingJsonPath("@.*"))
            .withRequestBody(equalToXml("<thing />"))
            .withRequestBody(matchingXPath("//thing"))
            .withRequestBody(containing("thin"))
            .withRequestBody(notContaining("stuff"))
            .withRequestBody(not(containing("thing")))
            .withRequestBody(matching(".*thing.*"))
            .withRequestBody(notMatching("^stuff.+"))
            .build();

    String json = Json.write(requestPattern);
    JSONAssert.assertEquals(ALL_BODY_PATTERNS_EXAMPLE, json, true);
  }

  static Matcher<ContentPattern<?>> valuePattern(
      final Class<? extends StringValuePattern> patternClass, final String expectedValue) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(ContentPattern<?> item, Description mismatchDescription) {
        return item.getClass().equals(patternClass) && item.getValue().equals(expectedValue);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(
            "a value pattern of type "
                + patternClass.getSimpleName()
                + " with expected value "
                + expectedValue);
      }
    };
  }

  static Matcher<ContentPattern<?>> notValuePattern(
      final Class<? extends StringValuePattern> patternClass,
      final StringValuePattern unexpectedPattern) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(ContentPattern<?> item, Description mismatchDescription) {
        return item.getClass().equals(patternClass)
            && item.getValue().equals(unexpectedPattern.expectedValue);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(
            "a value pattern of type "
                + patternClass.getSimpleName()
                + " with expected value "
                + unexpectedPattern.expectedValue);
      }
    };
  }
}
