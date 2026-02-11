/*
 * Copyright (C) 2012-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_LENGTH;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.CompressionType;
import com.github.tomakehurst.wiremock.common.entity.EmptyEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.Format;
import com.github.tomakehurst.wiremock.common.entity.JsonEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.SimpleStringEntityDefinition;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.wiremock.url.Path;

public class ResponseDefinitionTest {

  public static final ResponseDefinition ALL_NULLS_RESPONSE_DEFINITION =
      new ResponseDefinition(
          200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
          null, null, null);

  @Test
  public void copyProducesEqualObject() {
    ResponseDefinition response =
        new ResponseDefinition(
            222,
            null,
            WireMock.textEntity("blah").build(),
            null,
            null,
            "name.json",
            new HttpHeaders(httpHeader("thing", "thingvalue")),
            null,
            null,
            1112,
            null,
            null,
            "http://base.com",
            null,
            Fault.EMPTY_RESPONSE,
            List.of("transformer-1"),
            Parameters.one("name", "Jeff"),
            true);

    ResponseDefinition copiedResponse = copyOf(response);

    assertEquals(response, copiedResponse);
  }

  @Test
  public void copyPreservesConfiguredFlag() {
    ResponseDefinition response = ResponseDefinition.notConfigured();
    ResponseDefinition copiedResponse = copyOf(response);
    assertFalse(copiedResponse.wasConfigured(), "Should be not configured");
  }

  private static final String STRING_BODY =
      // language=JSON
      """
          {
            "status": 200,
            "body": "String content"
          }
          """;

  @Test
  public void correctlyUnmarshalsFromJsonWhenBodyIsAString() {
    ResponseDefinition responseDef = Json.read(STRING_BODY, ResponseDefinition.class);
    assertThat(responseDef.getBase64Body(), is(nullValue()));
    assertThat(responseDef.getJsonBody(), is(nullValue()));
    assertThat(responseDef.getBodyEntity(), instanceOf(SimpleStringEntityDefinition.class));
    assertThat(responseDef.getBodyEntity().getData(), is("String content"));
  }

  private static final String JSON_BODY =
      // language=JSON
      """
        {
              "status": 200,
              "jsonBody": {"name":"wirmock","isCool":true}
        }
        """;

  @Test
  public void correctlyUnmarshalsFromJsonWhenBodyIsJson() {
    ResponseDefinition responseDef = Json.read(JSON_BODY, ResponseDefinition.class);
    assertThat(responseDef.getBase64Body(), is(nullValue()));
    assertThat(responseDef.getBodyEntity(), instanceOf(JsonEntityDefinition.class));

    JsonNode jsonNode = Json.node("{\"name\":\"wirmock\",\"isCool\":true}");
    assertThat(responseDef.getJsonBody(), is(jsonNode));
  }

  @Test
  public void correctlyMarshalsToJsonWhenBodyIsAString() {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withSimpleBody("String content").build();

    assertThat(Json.write(responseDef), jsonEquals(STRING_BODY));
  }

  private static final byte[] BODY = new byte[] {1, 2, 3};
  private static final String BASE64_BODY = "AQID";
  private static final String BINARY_BODY =
      "{	        								        \n"
          + "		\"status\": 200,    				        \n"
          + "		\"base64Body\": \""
          + BASE64_BODY
          + "\"     \n"
          + "}											        ";

  @Test
  public void correctlyUnmarshalsFromJsonWhenBodyIsBinary() {
    ResponseDefinition responseDef = Json.read(BINARY_BODY, ResponseDefinition.class);
    assertThat(responseDef.getByteBody(), is(BODY));
  }

  @Test
  public void correctlyMarshalsToJsonWhenBodyIsBinary() {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withBase64Body(BASE64_BODY).build();

    String actualJson = Json.write(responseDef);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
            {
              "status": 200,
              "base64Body": "AQID"
            }
            """));
  }

  @Test
  void correctlySerialisesCompressedText() {
    String plain = "{\"id\":1}";
    byte[] gzipped = Gzip.gzip(plain);
    String base64 = Encoding.encodeBase64(gzipped);

    ResponseDefinition responseDef =
        responseDefinition()
            .withStatus(200)
            .withHeader("Content-Encoding", "gzip")
            .withHeader(CONTENT_LENGTH, String.valueOf(gzipped.length))
            .withHeader("Content-Type", "application/json")
            .withBody(gzipped)
            .build();

    String actualJson = Json.write(responseDef);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
                    {
                       "status": 200,
                       "headers": {
                         "Content-Encoding": "gzip",
                         "Content-Length": "28",
                         "Content-Type": "application/json"
                       },
                       "base64Body": "%s"
                     }
                    """
                .formatted(base64)));
  }

  @Test
  void correctlySerialisesCompressedTextInV4Style() {
    String plain = "{\"id\":1}";
    byte[] gzipped = Gzip.gzip(plain);
    String base64 = Encoding.encodeBase64(gzipped);

    ResponseDefinition responseDef =
        responseDefinition()
            .withStatus(200)
            .withHeader("Content-Encoding", "gzip")
            .withHeader(CONTENT_LENGTH, String.valueOf(gzipped.length))
            .withHeader("Content-Type", "application/json")
            .withBody(gzipped)
            .build();

    String actualJson = Json.write(responseDef, Json.V4StyleView.class);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
                    {
                       "status": 200,
                       "headers": {
                         "Content-Encoding": "gzip",
                         "Content-Length": "28",
                         "Content-Type": "application/json"
                       },
                       "body": {
                         "compression": "gzip",
                         "format": "json",
                         "base64Data": "%s"
                       }
                     }
                    """
                .formatted(base64)));
  }

  @Test
  void correctlyMarshalsJsonBodyInV4Style() {
    ResponseDefinition responseDef =
        responseDefinition()
            .withStatus(200)
            .withJsonBody(Json.node("{\"name\":\"wiremock\"}"))
            .build();

    String actualJson = Json.write(responseDef, Json.V4StyleView.class);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
            {
              "status": 200,
              "body": {
                "format": "json",
                "data": {
                  "name": "wiremock"
                }
              }
            }
            """));
  }

  @Test
  void correctlyMarshalsStringBodyInV4Style() {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withSimpleBody("Hello world").build();

    String actualJson = Json.write(responseDef, Json.V4StyleView.class);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
            {
              "status": 200,
              "body": {
                "data": "Hello world"
              }
            }
            """));
  }

  @Test
  void correctlyMarshalsBinaryBodyInV4Style() {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withBase64Body(BASE64_BODY).build();

    String actualJson = Json.write(responseDef, Json.V4StyleView.class);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
            {
              "status": 200,
              "body": {
                "base64Data": "AQID"
              }
            }
            """));
  }

  @Test
  void v4StyleExcludesBase64BodyJsonBodyBodyFileNameAndBodyMetadata() {
    String plain = "{\"id\":1}";
    byte[] gzipped = Gzip.gzip(plain);

    ResponseDefinition responseDef =
        responseDefinition()
            .withStatus(200)
            .withHeader("Content-Encoding", "gzip")
            .withHeader("Content-Type", "application/json")
            .withBody(gzipped)
            .build();

    String actualJson = Json.write(responseDef, Json.V4StyleView.class);

    assertThat(actualJson, not(containsString("base64Body")));
    assertThat(actualJson, not(containsString("jsonBody")));
    assertThat(actualJson, not(containsString("bodyFileName")));
  }

  @Test
  void correctlyMarshalsJsonBodyInPublicView() {
    ResponseDefinition responseDef =
        responseDefinition()
            .withStatus(200)
            .withJsonBody(Json.node("{\"name\":\"wiremock\"}"))
            .build();

    String actualJson = Json.write(responseDef);

    assertThat(
        actualJson,
        jsonEquals(
            // language=JSON
            """
            {
              "status": 200,
              "jsonBody": {
                "name": "wiremock"
              }
            }
            """));
  }

  @Test
  public void omitsResponseTransformerAttributesFromJsonWhenEmpty() {
    String json = Json.write(responseDefinition().withStatus(200).build());

    assertThat(json, not(containsString("transformers")));
    assertThat(json, not(containsString("transformerParameters")));
  }

  @Test
  public void removeProxyRequestHeadersListIsImmutable() {
    var removeProxyRequestHeaders = new ArrayList<String>();
    var builder1 = new ResponseDefinition.Builder();
    removeProxyRequestHeaders.add("header-1");
    builder1.setRemoveProxyRequestHeaders(removeProxyRequestHeaders);

    var responseDefinition1 = builder1.build();
    assertThat(responseDefinition1.getRemoveProxyRequestHeaders(), contains("header-1"));

    removeProxyRequestHeaders.clear();

    assertThat(responseDefinition1.getRemoveProxyRequestHeaders(), contains("header-1"));

    assertThrows(
        UnsupportedOperationException.class,
        () -> responseDefinition1.getRemoveProxyRequestHeaders().add("header-2"));

    assertThat(responseDefinition1.getRemoveProxyRequestHeaders(), contains("header-1"));

    builder1.getRemoveProxyRequestHeaders().clear();

    assertThat(responseDefinition1.getRemoveProxyRequestHeaders(), contains("header-1"));

    var builder2 = responseDefinition1.toBuilder();
    builder2.getRemoveProxyRequestHeaders().add("header-2");
    var responseDefinition2 = builder2.build();

    assertThat(responseDefinition1.getRemoveProxyRequestHeaders(), contains("header-1"));

    assertThat(
        responseDefinition2.getRemoveProxyRequestHeaders(), contains("header-1", "header-2"));
  }

  @Test
  public void transformersListIsImmutable() {
    var transformers = new ArrayList<String>();
    var builder1 = new ResponseDefinition.Builder();
    transformers.add("transformer-1");
    builder1.setTransformers(transformers);

    var responseDefinition1 = builder1.build();
    assertThat(responseDefinition1.getTransformers(), contains("transformer-1"));

    transformers.clear();

    assertThat(responseDefinition1.getTransformers(), contains("transformer-1"));

    assertThrows(
        UnsupportedOperationException.class,
        () -> responseDefinition1.getTransformers().add("transformer-2"));

    assertThat(responseDefinition1.getTransformers(), contains("transformer-1"));

    builder1.getTransformers().clear();

    assertThat(responseDefinition1.getTransformers(), contains("transformer-1"));

    var builder2 = responseDefinition1.toBuilder();
    builder2.getTransformers().add("transformer-2");
    var responseDefinition2 = builder2.build();

    assertThat(responseDefinition1.getTransformers(), contains("transformer-1"));

    assertThat(responseDefinition2.getTransformers(), contains("transformer-1", "transformer-2"));
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void headersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getHeaders(), notNullValue());
    assertThat(builder.getHeaders().size(), is(0));
    assertThrows(NullPointerException.class, () -> builder.setHeaders(null));
    assertThat(builder.getHeaders(), notNullValue());
    assertThat(builder.getHeaders().size(), is(0));
    assertThat(builder.build().getHeaders(), notNullValue());
    assertThat(builder.build().getHeaders().size(), is(0));
    var responseDefinition = ALL_NULLS_RESPONSE_DEFINITION;
    assertThat(responseDefinition.getHeaders(), notNullValue());
    assertThat(responseDefinition.getHeaders().size(), is(0));
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void additionalProxyRequestHeadersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getAdditionalProxyRequestHeaders(), notNullValue());
    assertThat(builder.getAdditionalProxyRequestHeaders().size(), is(0));
    assertThrows(NullPointerException.class, () -> builder.setAdditionalProxyRequestHeaders(null));
    assertThat(builder.getAdditionalProxyRequestHeaders(), notNullValue());
    assertThat(builder.getAdditionalProxyRequestHeaders().size(), is(0));
    assertThat(builder.build().getAdditionalProxyRequestHeaders(), notNullValue());
    assertThat(builder.build().getAdditionalProxyRequestHeaders().size(), is(0));
    var responseDefinition = ALL_NULLS_RESPONSE_DEFINITION;
    assertThat(responseDefinition.getAdditionalProxyRequestHeaders(), notNullValue());
    assertThat(responseDefinition.getAdditionalProxyRequestHeaders().size(), is(0));
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void removeProxyRequestHeadersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getRemoveProxyRequestHeaders(), empty());
    assertThrows(NullPointerException.class, () -> builder.setRemoveProxyRequestHeaders(null));
    assertThat(builder.getRemoveProxyRequestHeaders(), empty());
    assertThat(builder.build().getRemoveProxyRequestHeaders(), empty());
    assertThat(ALL_NULLS_RESPONSE_DEFINITION.getRemoveProxyRequestHeaders(), empty());
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void transformersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getTransformers(), empty());
    assertThrows(NullPointerException.class, () -> builder.setTransformers(null));
    assertThat(builder.getTransformers(), notNullValue());
    assertThat(builder.getTransformers().size(), is(0));
    assertThat(builder.build().getTransformers(), empty());
    assertThat(ALL_NULLS_RESPONSE_DEFINITION.getTransformers(), empty());
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  public void transformerParametersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getTransformerParameters(), notNullValue());
    assertThat(builder.getTransformerParameters().size(), is(0));
    assertThrows(NullPointerException.class, () -> builder.setTransformerParameters(null));
    assertThat(builder.getTransformerParameters(), notNullValue());
    assertThat(builder.getTransformerParameters().size(), is(0));
    assertThat(builder.build().getTransformerParameters(), notNullValue());
    assertThat(builder.build().getTransformerParameters().size(), is(0));
    assertThat(ALL_NULLS_RESPONSE_DEFINITION.getTransformerParameters(), notNullValue());
    assertThat(ALL_NULLS_RESPONSE_DEFINITION.getTransformerParameters().size(), is(0));
  }

  @Test
  public void builderCopiesExistingValues() {
    var responseDefinition =
        new ResponseDefinition(
            200,
            "my status message",
            WireMock.textEntity("my body").build(),
            null,
            null,
            null,
            new HttpHeaders(httpHeader("header-1", "h1v1", "h1v2")),
            new HttpHeaders(httpHeader("additional-header-1", "h1v1", "h1v2")),
            List.of("remove-header-1", "h1v1", "h1v2"),
            1000,
            new FixedDelayDistribution(2000),
            new ChunkedDribbleDelay(3, 200),
            "http://example.com",
            "my-prefix",
            Fault.EMPTY_RESPONSE,
            List.of("my-transformer"),
            Parameters.one("p-1", "p1v1"),
            true);

    var copy = responseDefinition.toBuilder().build();
    assertThat(copy, is(responseDefinition));
    assertThat(copy.getStatus(), is(200));
    assertThat(copy.getStatusMessage(), is("my status message"));
    assertThat(copy.getBodyEntity().getData(), is("my body"));
    assertThat(copy.getHeaders(), is(new HttpHeaders(httpHeader("header-1", "h1v1", "h1v2"))));
    assertThat(
        copy.getAdditionalProxyRequestHeaders(),
        is(new HttpHeaders(httpHeader("additional-header-1", "h1v1", "h1v2"))));
    assertThat(copy.getRemoveProxyRequestHeaders(), is(List.of("remove-header-1", "h1v1", "h1v2")));
    assertThat(copy.getFixedDelayMilliseconds(), is(1000));
    assertThat(copy.getDelayDistribution(), instanceOf(FixedDelayDistribution.class));
    assertThat(copy.getDelayDistribution().sampleMillis(), is(2000L));
    assertThat(copy.getChunkedDribbleDelay().getNumberOfChunks(), is(3));
    assertThat(copy.getChunkedDribbleDelay().getTotalDuration(), is(200));
    assertThat(copy.getProxyBaseUrl(), is("http://example.com"));
    assertThat(copy.getProxyUrlPrefixToRemove(), is(Path.parse("my-prefix")));
    assertThat(copy.getFault(), is(Fault.EMPTY_RESPONSE));
    assertThat(copy.getTransformers(), is(List.of("my-transformer")));
    assertThat(copy.getTransformerParameters(), is(Parameters.one("p-1", "p1v1")));
    assertThat(copy.wasConfigured(), is(true));
  }

  @Test
  void parsesMinimalNewStyleBody() {
    var json =
        // language=JSON
        """
            {
              "body": {
                "data": "{ \\"message\\": \\"Hello\\" }"
              }
            }
            """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    EntityDefinition body = responseDefinition.getBodyEntity();
    assertThat(body, notNullValue());
    assertThat(body, instanceOf(EntityDefinition.class));
    assertThat(body.getData(), is("{ \"message\": \"Hello\" }"));
    assertThat(body.isInline(), is(true));
  }

  @Test
  void parsesMaxmimalNewStyleBodyWithStringData() {
    var json =
        // language=JSON
        """
            {
              "body": {
                "data": "<my-data/>",
                "compression": "none",
                "encoding": "text",
                "format": "xml",
                "charset": "UTF-16"
              }
            }
            """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    EntityDefinition body = responseDefinition.getBodyEntity();
    assertThat(body, notNullValue());
    assertThat(body, instanceOf(EntityDefinition.class));

    assertThat(body.getDataAsString(), is("<my-data/>"));
    assertThat(body.isInline(), is(true));
    assertThat(body.getCompression(), is(CompressionType.NONE));
    assertThat(body.getFormat(), is(Format.XML));
    assertThat(body.getCharset(), is(StandardCharsets.UTF_16));
  }

  @Test
  void parsesNewStyleBodyWithJsonData() {
    var json =
        // language=JSON
        """
            {
              "body": {
                "data": {
                  "key": "value"
                },
                "encoding": "text"
              }
            }
            """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    EntityDefinition body = responseDefinition.getBodyEntity();
    assertThat(body, notNullValue());
    assertThat(body, instanceOf(JsonEntityDefinition.class));

    JsonEntityDefinition jsonBody = (JsonEntityDefinition) body;
    assertThat(jsonBody.getData(), instanceOf(JsonNode.class));
    assertThat(body.isInline(), is(true));
    assertThat(body.getCompression(), is(CompressionType.NONE));
    assertThat(body.getFormat(), is(Format.JSON));

    assertThat(jsonBody.getDataAsJson().get("key").textValue(), is("value"));
  }

  @Test
  void takesCharsetFromContentTypeHeaderWhenAvailableAndNotSetExplicitly() {
    var json =
        // language=JSON
        """
            {
              "headers": {
                "Content-Type": "text/plain; charset=UTF-16"
              },
              "body": {
                "data": "odd charset"
              }
            }
            """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    assertThat(responseDefinition.getBodyEntity().getCharset(), is(StandardCharsets.UTF_16));
  }

  @Test
  void takesContentEncodingFromHeaderWhenAvailable() {
    var json =
        // language=JSON
        """
            {
              "headers": {
                "Content-Encoding": "gzip"
              },
              "body": {
                "data": "compressed"
              }
            }
            """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    assertThat(responseDefinition.getBodyEntity().getCompression(), is(CompressionType.GZIP));
  }

  @Test
  void absentBodyFieldResultInEmptyBody() {
    var json =
        // language=JSON
        """
        {
          "status": 418
        }
        """;

    ResponseDefinition responseDefinition = Json.read(json, ResponseDefinition.class);

    assertThat(responseDefinition.getBodyEntity(), instanceOf(EmptyEntityDefinition.class));
  }

  @Test
  void bodyFileNameIsSerializedWhenSetAfterInlineBody() {
    // Mimic the recording flow: create with inline body, then set bodyFileName
    ResponseDefinition original =
        responseDefinition().withStatus(200).withBody("Proxied body").build();

    ResponseDefinition transformed = original.transform(rd -> rd.setBodyFileName("test-file.txt"));

    assertThat(transformed.getBodyFileName(), is("test-file.txt"));

    String json = Json.write(transformed);
    assertThat(json, containsString("\"bodyFileName\" : \"test-file.txt\""));
    assertThat(json, not(containsString("\"body\"")));
  }
}
