/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MockMultipart.mockPart;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class MultipartValuePatternTest {

  @Test
  public void matchesNameAndFileNameIndependentlyWhenNameIsSpecifiedFirst() {
    MultipartValuePattern pattern = aMultipart().withName("avatar").withFileName("pic.png").build();

    assertNameAndFileNameMatching(pattern);
  }

  @Test
  public void matchesNameAndFileNameIndependentlyWhenFileNameIsSpecifiedFirst() {
    MultipartValuePattern pattern = aMultipart().withFileName("pic.png").withName("avatar").build();

    assertNameAndFileNameMatching(pattern);
  }

  @Test
  public void matchesNameAndFileNameWithEqualDistanceRegardlessOfDslOrder() {
    MultipartValuePattern nameFirst =
        aMultipart().withName("avatar").withFileName("pic.png").build();
    MultipartValuePattern fileNameFirst =
        aMultipart().withFileName("pic.png").withName("avatar").build();
    MockMultipart matchingPart =
        mockPart()
            .name("avatar")
            .filename("pic.png")
            .header("Content-Disposition", "form-data; name=avatar; filename=pic.png");
    MockMultipart wrongNamePart =
        mockPart()
            .name("not-avatar")
            .filename("pic.png")
            .header("Content-Disposition", "form-data; name=not-avatar; filename=pic.png");
    MockMultipart wrongFileNamePart =
        mockPart()
            .name("avatar")
            .filename("not-pic.png")
            .header("Content-Disposition", "form-data; name=avatar; filename=not-pic.png");

    assertTrue(nameFirst.match(matchingPart).isExactMatch());
    assertTrue(fileNameFirst.match(matchingPart).isExactMatch());
    assertThat(
        nameFirst.match(wrongNamePart).getDistance(),
        is(fileNameFirst.match(wrongNamePart).getDistance()));
    assertThat(nameFirst.match(wrongNamePart).getDistance(), closeTo(0.25, 0.0001));
    assertThat(
        nameFirst.match(wrongFileNamePart).getDistance(),
        is(fileNameFirst.match(wrongFileNamePart).getDistance()));
    assertThat(nameFirst.match(wrongFileNamePart).getDistance(), closeTo(0.25, 0.0001));
  }

  @Test
  public void appliesExplicitContentDispositionPatternsRegardlessOfDslOrder() {
    assertContentDispositionHeaderIsApplied(
        aMultipart()
            .withHeader("Content-Disposition", containing("name=\"avatar\""))
            .withName("avatar")
            .withFileName("pic.png")
            .build());
    assertContentDispositionHeaderIsApplied(
        aMultipart()
            .withName("avatar")
            .withFileName("pic.png")
            .withHeader("Content-Disposition", containing("name=\"avatar\""))
            .build());
    assertContentDispositionHeaderIsApplied(
        aMultipart()
            .withHeader("Content-Disposition", containing("filename=\"pic.png\""))
            .withFileName("pic.png")
            .withName("avatar")
            .build());
    assertContentDispositionHeaderIsApplied(
        aMultipart()
            .withFileName("pic.png")
            .withName("avatar")
            .withHeader("Content-Disposition", containing("filename=\"pic.png\""))
            .build());
  }

  @Test
  public void appliesExplicitContentDispositionPatternsFromJson() {
    MultipartValuePattern namePattern =
        Json.read(
            """
            {
              "name": "avatar",
              "fileName": "pic.png",
              "matchingType": "ANY",
              "headers": {
                "Content-Disposition": { "contains": "name=\\\"avatar\\\"" }
              }
            }
            """,
            MultipartValuePattern.class);
    MultipartValuePattern fileNamePattern =
        Json.read(
            """
            {
              "name": "avatar",
              "fileName": "pic.png",
              "matchingType": "ANY",
              "headers": {
                "Content-Disposition": { "contains": "filename=\\\"pic.png\\\"" }
              }
            }
            """,
            MultipartValuePattern.class);

    assertContentDispositionHeaderIsApplied(namePattern);
    assertContentDispositionHeaderIsApplied(fileNamePattern);
  }

  @Test
  public void matchesNameWithoutHeaderOrBodyPatterns() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "name": "avatar",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(pattern.match(mockPart().name("avatar")).isExactMatch());
    assertFalse(pattern.match(mockPart().name("not-avatar")).isExactMatch());
  }

  @Test
  public void fallsBackToContentDispositionWhenPartNameIsUnavailable() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "name": "avatar",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(
        pattern
            .match(mockPart().header("Content-Disposition", "attachment; name=\"avatar\""))
            .isExactMatch());
    assertTrue(
        pattern
            .match(
                mockPart()
                    .name("")
                    .header("Content-Disposition", "attachment; name=avatar; filename=pic.png"))
            .isExactMatch());
    assertFalse(
        pattern
            .match(
                mockPart()
                    .name("not-avatar")
                    .header("Content-Disposition", "attachment; name=avatar"))
            .isExactMatch());
    assertFalse(
        pattern
            .match(
                mockPart()
                    .filename("avatar")
                    .header("Content-Disposition", "attachment; filename=\"avatar\""))
            .isExactMatch());
    assertFalse(pattern.match(mockPart()).isExactMatch());
  }

  @Test
  public void fallsBackToContentDispositionWhenPartFileNameIsUnavailable() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "fileName": "pic.png",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(
        pattern
            .match(
                mockPart()
                    .header("Content-Disposition", "attachment; name=avatar; filename=pic.png"))
            .isExactMatch());
    assertFalse(pattern.match(mockPart()).isExactMatch());
  }

  @Test
  public void fallsBackAcrossMultipleContentDispositionValuesAndCaseInsensitiveParameters() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "name": "avatar",
              "fileName": "pic.png",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(
        pattern
            .match(
                mockPart()
                    .header(
                        "Content-Disposition",
                        "form-data; ignored=value",
                        "attachment; NaMe=avatar; FiLeNaMe=\"pic.png\""))
            .isExactMatch());
  }

  @Test
  public void treatsNullContentDispositionHeaderAsNoMatchDuringFallback() {
    MultipartValuePattern namePattern = aMultipart().withName("avatar").build();
    MultipartValuePattern fileNamePattern = aMultipart().withFileName("pic.png").build();
    Request.Part part = partReturningNullHeader();

    assertFalse(namePattern.match(part).isExactMatch());
    assertFalse(fileNamePattern.match(part).isExactMatch());
  }

  @Test
  public void preservesDistanceWhenNameIsAbsentOrEmpty() {
    MockMultipart part = mockPart().header("X-Test", "actual");

    assertThat(headerOnlyPattern(null).match(part).getDistance(), closeTo(1.0 / 3.0, 0.0001));
    assertThat(headerOnlyPattern("").match(part).getDistance(), closeTo(1.0 / 3.0, 0.0001));
  }

  @Test
  public void ignoresEmptyNameAndFileNameConstraints() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "name": "",
              "fileName": "",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(pattern.match(mockPart().name("avatar").filename("pic.png")).isExactMatch());
  }

  @Test
  public void matchesFileNameWithoutHeaderOrBodyPatterns() {
    MultipartValuePattern pattern =
        Json.read(
            """
            {
              "fileName": "pic.png",
              "matchingType": "ANY"
            }
            """,
            MultipartValuePattern.class);

    assertTrue(pattern.match(mockPart().filename("pic.png")).isExactMatch());
    assertFalse(pattern.match(mockPart().filename("not-pic.png")).isExactMatch());
  }

  @Test
  public void deserialisesCorrectlyWhenNoBodyOrHeaderMatchersPresent() {
    String serializedPattern = "{\n" + "    \"matchingType\": \"ANY\"" + "}";

    MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);
    assertTrue(pattern.isMatchAny());
    assertFalse(pattern.isMatchAll());
  }

  @Test
  public void deserialisesCorrectlyWithTypeAllAndSingleHeaderMatcher() {
    String serializedPattern =
        "{                                           \n"
            + "    \"matchingType\": \"ALL\",              \n"
            + "    \"headers\": {                          \n"
            + "        \"Content-Disposition\": {\n        \n"
            + "            \"contains\": \"name=\\\"part1\\\"\"\n"
            + "        }\n"
            + "    }"
            + "}";

    MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

    StringValuePattern headerPattern =
        ((SingleMatchMultiValuePattern) pattern.getHeaders().get("Content-Disposition"))
            .getValuePattern();
    assertThat(headerPattern, instanceOf(ContainsPattern.class));
    assertThat(headerPattern.getValue(), is("name=\"part1\""));

    assertNull(pattern.getBodyPatterns());
    assertTrue(pattern.isMatchAll());
    assertFalse(pattern.isMatchAny());
  }

  @Test
  public void deserialisesCorrectlyWithSingleJsonBodyMatcer() throws JSONException {
    String expectedJson = "{ \"someKey\": \"someValue\" }";
    String serializedPattern =
        "{                                                \n"
            + "    \"matchingType\": \"ANY\",                   \n"
            + "    \"bodyPatterns\": [                          \n"
            + "        { \"equalToJson\": "
            + expectedJson
            + " }\n"
            + "    ]\n"
            + "}";

    MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

    JSONAssert.assertEquals(pattern.getBodyPatterns().get(0).getExpected(), expectedJson, false);

    assertNull(pattern.getHeaders());
    assertTrue(pattern.isMatchAny());
    assertFalse(pattern.isMatchAll());
  }

  @Test
  public void deserialisesCorrectlyWithANYMatchTypeWithMultipleHeaderAndBodyMatchers()
      throws JSONException {
    String expectedJson = "{ \"someKey\": \"someValue\" }";
    String serializedPattern =
        "{\n"
            + "    \"matchingType\": \"ANY\",\n"
            + "    \"headers\": {\n"
            + "        \"Content-Disposition\": \n"
            + "            {\n"
            + "                \"contains\": \"name=\\\"part1\\\"\"\n"
            + "            }\n"
            + "        ,\n"
            + "        \"Content-Type\": \n"
            + "            {\n"
            + "                \"contains\": \"application/json\"\n"
            + "            }"
            + "        \n"
            + "    },\n"
            + "    \"bodyPatterns\": [\n"
            + "        {\n"
            + "            \"equalToJson\": "
            + expectedJson
            + "\n"
            + "        }\n"
            + "    ]\n"
            + "}";

    MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

    assertThat(
        pattern.getBodyPatterns().get(0).getExpected(), WireMatchers.equalToJson(expectedJson));

    SingleMatchMultiValuePattern contentTypeHeaderPattern =
        (SingleMatchMultiValuePattern) pattern.getHeaders().get("Content-Type");
    assertThat(contentTypeHeaderPattern.getValuePattern(), instanceOf(ContainsPattern.class));
    assertThat(contentTypeHeaderPattern.getValuePattern().getExpected(), is("application/json"));
    assertTrue(pattern.isMatchAny());
    assertFalse(pattern.isMatchAll());
  }

  @Test
  public void deserialisesCorrectlyWithHeadersAndBinaryBody() {
    String expectedBinary = "RG9jdW1lbnQgYm9keSBjb250ZW50cw==";
    String serializedPattern =
        "{\n"
            + "    \"name\": \"my_part_name\",\n"
            + "    \"matchingType\": \"ALL\",\n"
            + "    \"headers\": {\n"
            + "        \"Content-Disposition\": \n"
            + "            {\n"
            + "                \"contains\": \"name=\\\"file\\\"\"\n"
            + "            }\n"
            + "        ,\n"
            + "        \"Content-Type\": \n"
            + "            {\n"
            + "                \"equalTo\": \"application/octet-stream\"\n"
            + "            }\n"
            + "        \n"
            + "    },\n"
            + "    \"bodyPatterns\": [\n"
            + "        {\n"
            + "            \"binaryEqualTo\": \""
            + expectedBinary
            + "\"\n"
            + "        }\n"
            + "    ]\n"
            + "}";

    MultipartValuePattern pattern = Json.read(serializedPattern, MultipartValuePattern.class);

    assertThat(pattern.getName(), is("my_part_name"));
    assertEquals(pattern.getBodyPatterns().get(0).getExpected(), expectedBinary);
    assertThat(
        ((SingleMatchMultiValuePattern) pattern.getHeaders().get("Content-Type"))
            .getValuePattern()
            .getExpected(),
        is("application/octet-stream"));
    assertTrue(pattern.isMatchAll());
    assertFalse(pattern.isMatchAny());
  }

  @Test
  public void serialisesCorrectlyWithMultipleHeaderAndBodyMatchers() {
    MultipartValuePattern pattern =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": 123 }"))
            .build();

    String json = Json.write(pattern);

    assertThat(
        json,
        WireMatchers.equalToJson(
            "{\n"
                + "  \"name\" : \"title\",\n"
                + "  \"matchingType\" : \"ANY\",\n"
                + "  \"headers\" : {\n"
                + "    \"X-First-Header\" : {\n"
                + "      \"equalTo\" : \"One\"\n"
                + "    },\n"
                + "    \"X-Second-Header\" : {\n"
                + "      \"matches\" : \".*2\"\n"
                + "    }\n"
                + "  },\n"
                + "  \"bodyPatterns\" : [ {\n"
                + "    \"equalToJson\" : \"{ \\\"thing\\\": 123 }\"\n"
                + "  } ]\n"
                + "}"));
  }

  @Test
  public void serialisesDedicatedFieldsWithoutAnImplicitContentDispositionPattern() {
    JsonNode generatedPattern =
        Json.node(Json.write(aMultipart().withName("avatar").withFileName("pic.png").build()));
    JsonNode explicitHeaderPattern =
        Json.node(
            Json.write(
                aMultipart()
                    .withName("avatar")
                    .withFileName("pic.png")
                    .withHeader("Content-Disposition", containing("form-data"))
                    .build()));

    assertThat(generatedPattern.get("name").textValue(), is("avatar"));
    assertThat(generatedPattern.get("fileName").textValue(), is("pic.png"));
    assertFalse(generatedPattern.has("headers"));
    assertThat(
        explicitHeaderPattern.get("headers").get("Content-Disposition").get("contains").textValue(),
        is("form-data"));
  }

  @Test
  public void equalsShouldReturnTrueOnSameObject() {
    MultipartValuePattern pattern =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": 123 }"))
            .build();

    assertThat(pattern.equals(pattern), is(true));
    assertEquals(pattern.hashCode(), pattern.hashCode());
  }

  @Test
  public void equalsShouldReturnTrueOnIdenticalButNotSameObjects() {
    MultipartValuePattern patternA =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": 123 }"))
            .build();

    MultipartValuePattern patternB =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": 123 }"))
            .build();

    assertThat(patternA.equals(patternB), is(true));
    assertEquals(patternA.hashCode(), patternB.hashCode());
  }

  @Test
  public void equalsShouldReturnFalseOnDifferentObjects() {
    MultipartValuePattern patternA =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": 123 }"))
            .build();

    MultipartValuePattern patternB =
        aMultipart()
            .withName("anotherTitle")
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(equalToJson("{ \"thing\": \"abc\" }"))
            .build();

    assertThat(patternA.equals(patternB), is(false));
    assertNotEquals(patternA.hashCode(), patternB.hashCode());
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    MultipartValuePattern patternA =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(binaryEqualTo("RG9jdW1lbnQgYm9keSBjb250ZW50cw=="))
            .build();

    MultipartValuePattern patternB =
        aMultipart()
            .withName("title")
            .withHeader("X-First-Header", equalTo("One"))
            .withHeader("X-Second-Header", matching(".*2"))
            .withBody(binaryEqualTo("RG9jdW1lbnQgYm9keSBjb250ZW50cw=="))
            .build();

    MultipartValuePattern patternC =
        aMultipart()
            .withName("Description")
            .withHeader("X-First-Header", equalTo("Second"))
            .withBody(binaryEqualTo("SGVsbG9Xb3JsZA=="))
            .build();

    assertEquals(patternA, patternB);
    assertEquals(patternA.hashCode(), patternB.hashCode());
    assertEquals(patternB, patternA);
    assertEquals(patternB.hashCode(), patternA.hashCode());
    assertNotEquals(patternA, patternC);
    assertNotEquals(patternA.hashCode(), patternC.hashCode());
    assertNotEquals(patternB, patternC);
    assertNotEquals(patternB.hashCode(), patternC.hashCode());
  }

  private static void assertNameAndFileNameMatching(MultipartValuePattern pattern) {
    assertTrue(pattern.match(part("avatar", "pic.png")).isExactMatch());
    assertFalse(pattern.match(part("not-avatar", "pic.png")).isExactMatch());
    assertFalse(pattern.match(part("avatar", "not-pic.png")).isExactMatch());

    MockMultipart partWithNameInFileName =
        mockPart()
            .name("not-avatar")
            .filename("avatar")
            .header("Content-Disposition", "form-data; name=\"not-avatar\"; filename=\"avatar\"");
    assertFalse(pattern.match(partWithNameInFileName).isExactMatch());
  }

  private static MockMultipart part(String name, String filename) {
    return mockPart()
        .name(name)
        .filename(filename)
        .header(
            "Content-Disposition",
            "form-data; name=\"" + name + "\"; filename=\"" + filename + "\"");
  }

  private static MultipartValuePattern headerOnlyPattern(String name) {
    return new MultipartValuePattern(
        name,
        null,
        MultipartValuePattern.MatchingType.ANY,
        Map.of("X-Test", MultiValuePattern.of(absent())),
        null);
  }

  private static void assertContentDispositionHeaderIsApplied(MultipartValuePattern pattern) {
    MatchResult result =
        pattern.match(
            mockPart()
                .name("avatar")
                .filename("pic.png")
                .header("Content-Disposition", "form-data; name=avatar; filename=pic.png"));

    assertFalse(result.isExactMatch());
    assertThat(result.getDistance(), closeTo(0.25, 0.0001));
  }

  private static Request.Part partReturningNullHeader() {
    return new Request.Part() {
      @Override
      public String getName() {
        return null;
      }

      @Override
      public String getFileName() {
        return null;
      }

      @Override
      public HttpHeader getHeader(String name) {
        return null;
      }

      @Override
      public HttpHeaders getHeaders() {
        return HttpHeaders.noHeaders();
      }

      @Override
      public Entity getBodyEntity() {
        return null;
      }
    };
  }
}
