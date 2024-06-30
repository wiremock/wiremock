/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Json.prettyPrint;
import static com.github.tomakehurst.wiremock.common.Strings.normaliseLineBreaks;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.matching.MockMultipart.mockPart;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalsMultiLine;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.EagerMatchResult;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.WeightedAggregateMatchResult;
import com.github.tomakehurst.wiremock.matching.WeightedMatchResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

class PlainTextDiffRendererTest {

  PlainTextDiffRenderer diffRenderer;

  @BeforeEach
  void init() {
    diffRenderer =
        new PlainTextDiffRenderer(
            Map.of(
                "my-custom-matcher", new MyCustomMatcher(),
                "self-describing-custom-matcher", new SelfDescribingCustomMatcher(),
                "weighted-self-describing-custom-matcher", new WeightedDescribingCustomMatcher()));
  }

  @Test
  void rendersWithDifferingUrlHeaderAndJsonBody() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName(
                    "The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
                .withHeader("X-My-Header", containing("correct value"))
                .withHeader("Accept", matching("text/plain.*"))
                .withRequestBody(
                    equalToJson(
                        "{     \n"
                            + "    \"thing\": {               \n"
                            + "        \"stuff\": [1, 2, 3]   \n"
                            + "    }                          \n"
                            + "}"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thin")
                .header("X-My-Header", "wrong value")
                .header("Accept", "text/plain")
                .body(
                    "{                        \n"
                        + "    \"thing\": {           \n"
                        + "        \"nothing\": {}    \n"
                        + "    }                      \n"
                        + "}"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_ascii.txt")));
  }

  @Test
  void rendersWithDifferingCookies() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Cookie diff")
                .withCookie("Cookie_1", containing("one value"))
                .withCookie("Second_Cookie", matching("cookie two value [0-9]*"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .cookie("Cookie_1", "zero value")
                .cookie("Second_Cookie", "cookie two value 123"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_cookies.txt")));
  }

  @Test
  void rendersWithDifferingQueryParameters() {
    Diff diff =
        new Diff(
            get(urlPathEqualTo("/thing"))
                .withName("Query params diff")
                .withQueryParam("one", equalTo("1"))
                .withQueryParam("two", containing("two things"))
                .withQueryParam("three", matching("[a-z]{5}"))
                .build(),
            mockRequest().method(GET).url("/thing?one=2&two=wrong%20things&three=abcde"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_query.txt")));
  }

  @Test
  void rendersWithDifferingFormParameters() {
    Diff diff =
        new Diff(
            put(urlPathEqualTo("/thing"))
                .withName("Query params diff")
                .withFormParam("one", equalTo("1"))
                .withFormParam("two", containing("two things"))
                .withFormParam("three", matching("[a-z]{5}"))
                .build(),
            mockRequest()
                .method(PUT)
                .url("/thing")
                .formParameters(getFormParameters())
                .header("Content-Type", "application/x-www-form-urlencoded"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_form.txt")));
  }

  private Map<String, FormParameter> getFormParameters() {
    Map<String, FormParameter> formParameters = new HashMap<>();
    formParameters.put("one", new FormParameter("one", List.of("2")));
    formParameters.put("two", new FormParameter("two", List.of("wrong things")));
    formParameters.put("three", new FormParameter("three", List.of("abcde")));
    return formParameters;
  }

  @Test
  void wrapsLargeJsonBodiesAppropriately() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName(
                    "The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
                .withHeader("Accept", equalTo("text/plain"))
                .withRequestBody(
                    equalToJson(
                        "{\n"
                            + "  \"one\": {\n"
                            + "    \"two\": {\n"
                            + "      \"three\": {\n"
                            + "        \"four\": {\n"
                            + "          \"five\": {\n"
                            + "            \"six\": \"superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless_superduperlongvaluethatshouldwrapokregardless\"\n"
                            + "          }\n"
                            + "        }\n"
                            + "      }\n"
                            + "    }\n"
                            + "  }\n"
                            + "}"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .header("Accept", "text/plain")
                .body(
                    prettyPrint(
                        "{\n"
                            + "  \"one\": {\n"
                            + "    \"two\": {\n"
                            + "      \"three\": {\n"
                            + "        \"four\": {\n"
                            + "          \"five\": {\n"
                            + "            \"six\": \"totally_the_wrong_value\"\n"
                            + "          }\n"
                            + "        }\n"
                            + "      }\n"
                            + "    }\n"
                            + "  }\n"
                            + "}")));

    String output = diffRenderer.render(diff);

    // Ugh. The joys of Microsoft's line ending innovations.
    String expected =
        System.getProperty("os.name").startsWith("Windows")
            ? file("not-found-diff-sample_large_json_windows.txt")
            : file("not-found-diff-sample_large_json.txt");

    System.out.println("expected:\n" + expected);
    System.out.println("actual:\n" + output);

    assertThat(normaliseLineBreaks(output), equalsMultiLine(expected));
  }

  @Test
  @DisabledForJreRange(min = JRE.JAVA_11, disabledReason = "Wrap differs per JRE")
  void wrapsLargeXmlBodiesAppropriatelyJre8() {
    String output = wrapsLargeXmlBodiesAppropriately();
    assertThat(output, equalsMultiLine(file("not-found-diff-sample_large_xml_jre8.txt")));
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_11, disabledReason = "Wrap differs per JRE")
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void wrapsLargeXmlBodiesAppropriatelyJre11() {
    String output = wrapsLargeXmlBodiesAppropriately();
    assertThat(output, equalsMultiLine(file("not-found-diff-sample_large_xml_jre11.txt")));
  }

  @Test
  @EnabledForJreRange(min = JRE.JAVA_11, disabledReason = "Wrap differs per JRE")
  @EnabledOnOs(value = OS.WINDOWS, disabledReason = "Wrap differs per OS")
  void wrapsLargeXmlBodiesAppropriatelyJre11Windows() {
    String output = wrapsLargeXmlBodiesAppropriately();

    String expected = file("not-found-diff-sample_large_xml_jre11_windows.txt");
    System.out.println("expected:\n" + expected);
    System.out.println("output:\n" + output);
    assertThat(output, equalsMultiLine(expected));
  }

  private String wrapsLargeXmlBodiesAppropriately() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName(
                    "The post stub with a really long name that ought to wrap and let us see exactly how that looks when it is done")
                .withRequestBody(
                    equalToXml(
                        "<deep-things>\n"
                            + "  <thing id=\"1\">\n"
                            + "    <thing id=\"2\">\n"
                            + "      <thing id=\"3\">\n"
                            + "        <thing id=\"4\">\n"
                            + "          <thing id=\"5\">\n"
                            + "            <thing id=\"6\">\n"
                            + "              Super wrong bit of text that should push it way over the length limit!\n"
                            + "            </thing>\n"
                            + "          </thing>\n"
                            + "        </thing>\n"
                            + "      </thing>\n"
                            + "    </thing>\n"
                            + "  </thing>\n"
                            + "</deep-things>"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .body(
                    "<deep-things>\n"
                        + "  <thing id=\"1\">\n"
                        + "    <thing id=\"2\">\n"
                        + "      <thing id=\"3\">\n"
                        + "        <thing id=\"4\">\n"
                        + "          <thing id=\"5\">\n"
                        + "            <thing id=\"6\">\n"
                        + "              Super long bit of text that should push it way over the length limit!\n"
                        + "            </thing>\n"
                        + "          </thing>\n"
                        + "        </thing>\n"
                        + "      </thing>\n"
                        + "    </thing>\n"
                        + "  </thing>\n"
                        + "</deep-things>"));

    String output = diffRenderer.render(diff);
    System.out.println(output);
    return output;
  }

  @Test
  void showsMissingHeaderMessage() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Missing header stub")
                .withHeader("X-My-Header", equalTo("correct value"))
                .build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_missing_header.txt")));
  }

  @Test
  void showsJsonPathMismatch() {
    Diff diff =
        new Diff(
            post("/thing")
                .withRequestBody(matchingJsonPath("$..six"))
                .withRequestBody(matchingJsonPath("$..seven"))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .body(
                    prettyPrint(
                        "{\n"
                            + "  \"one\": {\n"
                            + "    \"two\": {\n"
                            + "      \"three\": {\n"
                            + "        \"four\": {\n"
                            + "          \"five\": {\n"
                            + "            \"six\": \"match this\"\n"
                            + "          }\n"
                            + "        }\n"
                            + "      }\n"
                            + "    }\n"
                            + "  }\n"
                            + "}")));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    String expected = file("not-found-diff-sample_json-path.txt");
    assertThat(output, equalsMultiLine(expected));
  }

  @Test
  void showsXPathWithSubMatchMismatch() {
    Diff diff =
        new Diff(
            post("/thing").withRequestBody(matchingXPath("//thing/text()", equalTo("two"))).build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .body("<stuff>\n" + "    <thing>one</thing>\n" + "</stuff>"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    String expected = file("not-found-diff-sample_xpath-with-submatch.txt");
    assertThat(output, equalsMultiLine(expected));
  }

  @Test
  void showsUrlRegexUnescapedMessage() {
    Diff diff =
        new Diff(
            get(urlMatching("thing?query=value")).build(), mockRequest().method(GET).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_url-pattern.txt")));
  }

  @Test
  void showsUrlTemplateNonMatchMessage() {
    Diff diff =
        new Diff(
            get(urlPathTemplate("/contacts/{contactId}")).build(),
            mockRequest().method(GET).url("/contracts/12345"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_url-template.txt")));
  }

  @Test
  void showsUrlPathParametersNonMatchMessage() {
    Diff diff =
        new Diff(
            get(urlPathTemplate("/contacts/{contactId}"))
                .withPathParam("contactId", equalTo("123"))
                .build(),
            mockRequest().method(GET).url("/contacts/345"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_url-path-parameters.txt")));
  }

  @Test
  void showsMultipartDifference() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Multipart request body stub")
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("part_one")
                        .withHeader("X-My-Stuff", containing("stuff_parts"))
                        .withBody(matching("Some expected text.*")))
                .withMultipartRequestBody(
                    aMultipart()
                        .withHeader("X-More", containing("stuff_parts"))
                        .withBody(equalTo("Correct body")))
                .build(),
            mockRequest()
                .method(POST)
                .url("/thing")
                .header("Content-Type", "multipart/form-data")
                .part(
                    mockPart()
                        .name("part_one")
                        .header("X-My-Stuff", "wrong value")
                        .body("Wrong body"))
                .part(mockPart().name("part_two").body("Correct body")));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_multipart.txt")));
  }

  @Test
  void showsErrorInDiffWhenMultipartExpectedButNotSent() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Multipart request body stub")
                .withMultipartRequestBody(
                    aMultipart()
                        .withHeader("X-My-Stuff", containing("stuff_parts"))
                        .withBody(matching("Some expected text.*")))
                .build(),
            mockRequest().method(POST).url("/thing").body("Non-multipart body"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_no-multipart.txt")));
  }

  @Test
  void showsErrorInDiffWhenInlineCustomMatcherNotSatisfiedInMixedStub() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Standard and custom matched stub")
                .andMatching(value -> MatchResult.noMatch())
                .build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_mixed-matchers.txt")));
  }

  @Test
  void showsErrorInDiffWhenNamedCustomMatcherNotSatisfiedInMixedStub() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Standard and custom matched stub")
                .andMatching("my-custom-matcher", Parameters.one("myVal", "present"))
                .build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(
        output, equalsMultiLine(file("not-found-diff-sample_mixed-matchers-named-custom.txt")));
  }

  @Test
  void showsErrorInDiffWhenSelfDescribingNamedCustomMatcherNotSatisfiedInMixedStub() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Standard and custom matched stub")
                .andMatching("self-describing-custom-matcher", Parameters.one("myVal", "present"))
                .build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(
        output,
        equalsMultiLine(
            file("not-found-diff-sample_mixed-matchers-self-describing-named-custom.txt")));
  }

  @Test
  void
      showsErrorInDiffWhenWeightedSelfDescribingNamedCustomMatcherNotSatisfiedInMixedStubDoesNotShowCustomDiff() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("Standard and weighted custom matched stub")
                .andMatching(
                    "weighted-self-describing-custom-matcher", Parameters.one("myVal", "present"))
                .build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(
        output,
        equalsMultiLine(file("not-found-diff-sample_mixed-matchers-weighted-named-custom.txt")));
  }

  @Test
  void showsErrorInDiffWhenExactMatchForMultipleValuesInQueryParamNotSatisfiedInStub() {
    Diff diff =
        new Diff(
            get(urlPathEqualTo("/thing")).withQueryParam("q", havingExactly("1", "2", "3")).build(),
            mockRequest().method(GET).url("/thing?q=2"));

    String output = diffRenderer.render(diff);
    assertThat(
        output,
        equalsMultiLine(
            file("not-found-diff-sample_exactmatch-for-multiple-values-query-param.txt")));
  }

  @Test
  void showsErrorInDiffWhenIncludesMatchForMultipleValuesInQueryParamNotSatisfiedInStub() {
    Diff diff =
        new Diff(
            get(urlPathEqualTo("/thing")).withQueryParam("q", including("1", "2", "3")).build(),
            mockRequest().method(GET).url("/thing?q=1"));

    String output = diffRenderer.render(diff);
    assertThat(
        output,
        equalsMultiLine(
            file("not-found-diff-sample_includematch-for-multiple-values-query-param.txt")));
  }

  @Test
  void showsErrorInDiffWhenExactMatchForMultipleValuesInHeaderNotSatisfiedInStub() {
    Diff diff =
        new Diff(
            get(urlPathEqualTo("/thing")).withHeader("q", havingExactly("1", "2", "3")).build(),
            mockRequest().method(GET).url("/thing").header("q", "1"));

    String output = diffRenderer.render(diff);
    assertThat(
        output,
        equalsMultiLine(file("not-found-diff-sample_exactmatch-for-multiple-values-header.txt")));
  }

  @Test
  void showsErrorInDiffWhenIncludesMatchForMultipleValuesInHeaderNotSatisfiedInStub() {
    Diff diff =
        new Diff(
            get(urlPathEqualTo("/thing")).withHeader("q", including("1", "2", "3")).build(),
            mockRequest().method(GET).url("/thing").header("q", "1"));

    String output = diffRenderer.render(diff);
    assertThat(
        output,
        equalsMultiLine(file("not-found-diff-sample_includematch-for-multiple-values-header.txt")));
  }

  @Test
  void showsAppropriateErrorInDiffWhenCustomMatcherIsUsedExclusively() {
    Diff diff =
        new Diff(
            requestMatching(value -> MatchResult.noMatch()).build(),
            mockRequest().method(POST).url("/thing"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_only-custom_matcher.txt")));
  }

  @Test
  void handlesUrlsWithQueryStringAndNoPath() {
    Diff diff =
        new Diff(
            newRequestPattern(GET, urlMatching("/?q=correct")).build(),
            mockRequest().method(GET).url("/q=wrong"));

    String output = diffRenderer.render(diff);
    System.out.println(output);

    assertThat(output, equalsMultiLine(file("not-found-diff-sample_no-path.txt")));
  }

  @Test
  void showsErrorInDiffWhenBodyDoesNotMatchJsonSchema() {
    Diff diff =
        new Diff(
            post("/thing")
                .withName("JSON schema stub")
                .withRequestBody(matchingJsonSchema(file("schema-validation/new-pet.schema.json")))
                .build(),
            mockRequest()
                .url("/thing")
                .method(POST)
                .body(file("schema-validation/new-pet.invalid.json")));

    String output = diffRenderer.render(diff);

    assertThat(
        normaliseLineBreaks(output),
        equalsMultiLine(file("not-found-diff-sample_json-schema.txt")));
  }

  @Test
  void showsErrorInDiffWhenBodyIsEmptyAndPathExpressionResult() {
    Diff diff =
        new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(matchingJsonPath("$.accountNum", equalTo("1234")))
                .build(),
            mockRequest().url("/thing").body(""));

    String output = diffRenderer.render(diff);

    assertThat(
        normaliseLineBreaks(output),
        equalsMultiLine(file("not-found-diff-sample_json-path-no-body.txt")));
  }

  @Test
  void showsErrorInDiffWhenBodyIsNotJsonAndPathExpressionResult() {
    Diff diff =
        new Diff(
            newRequestPattern(ANY, urlEqualTo("/thing"))
                .withRequestBody(matchingJsonPath("$.accountNum", equalTo("1234")))
                .build(),
            mockRequest().url("/thing").body("not json"));

    String output = diffRenderer.render(diff);

    assertThat(
        normaliseLineBreaks(output),
        equalsMultiLine(file("not-found-diff-sample_json-path-body-not-json.txt")));
  }

  @Test
  void doesNotIncorrectlyShowUrlPathParametersNonMatchMessage() {
    Diff diff =
        new Diff(
            get(urlPathTemplate("/contacts/{contactId}"))
                .withPathParam("contactId", equalTo("123"))
                .withHeader("Authorization", equalTo("Token 456"))
                .build(),
            mockRequest().method(GET).url("/contacts/123").header("Authorization", "Token 789"));

    String output = diffRenderer.render(diff);

    assertThat(
        output, equalsMultiLine(file("not-found-diff-sample_no_path_parameter_message.txt")));
  }

  public static class MyCustomMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      parameters.getString("myVal"); // Ensure we're getting passed parameters as expected
      return MatchResult.noMatch();
    }

    @Override
    public String getName() {
      return "my-custom-matcher";
    }
  }

  public static class SelfDescribingCustomMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      parameters.getString("myVal"); // Ensure we're getting passed parameters as expected
      final MatchResult.DiffDescription diffDescription =
          new MatchResult.DiffDescription(
              "Property a: foo",
              "Property a: bar",
              "Not matched because of property a not matching");
      final MatchResult.DiffDescription diffDescription2 =
          new MatchResult.DiffDescription(
              "Property b: foo",
              "Property b: bar",
              "Not matched because of property b not matching");
      return new EagerMatchResult(1, List.of(), List.of(diffDescription, diffDescription2));
    }

    @Override
    public String getName() {
      return "self-describing-custom-matcher";
    }
  }

  public static class WeightedDescribingCustomMatcher extends RequestMatcherExtension {

    @Override
    public MatchResult match(Request request, Parameters parameters) {
      parameters.getString("myVal"); // Ensure we're getting passed parameters as expected
      final MatchResult.DiffDescription diffDescription =
          new MatchResult.DiffDescription("Expected x", "Found y", "Not matched due to x != y");
      final MatchResult.DiffDescription diffDescription2 =
          new MatchResult.DiffDescription("Expected a", "Found b", "Not matched due to a != b");
      final MatchResult.DiffDescription diffDescription3 =
          new MatchResult.DiffDescription("Expected c", "Found d", "Not matched due to c != d");
      return new WeightedAggregateMatchResult(
          List.of(
              WeightedMatchResult.weight(
                  new EagerMatchResult(1, List.of(), List.of(diffDescription, diffDescription2))),
              WeightedMatchResult.weight(
                  new EagerMatchResult(1, List.of(), List.of(diffDescription3)))));
    }

    @Override
    public String getName() {
      return "weighted-self-describing-custom-matcher";
    }
  }
}
