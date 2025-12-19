/*
 * Copyright (C) 2012-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.FixedDelayDistribution;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ResponseDefinitionTest {

  @Test
  public void copyProducesEqualObject() {
    ResponseDefinition response =
        new ResponseDefinition(
            222,
            null,
            "blah",
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

    assertTrue(response.equals(copiedResponse));
  }

  @Test
  public void copyPreservesConfiguredFlag() {
    ResponseDefinition response = ResponseDefinition.notConfigured();
    ResponseDefinition copiedResponse = copyOf(response);
    assertFalse(copiedResponse.wasConfigured(), "Should be not configured");
  }

  private static final String STRING_BODY =
      "{	        								\n"
          + "		\"status\": 200,    				\n"
          + "		\"body\": \"String content\" 		\n"
          + "}											";

  @Test
  public void correctlyUnmarshalsFromJsonWhenBodyIsAString() {
    ResponseDefinition responseDef = Json.read(STRING_BODY, ResponseDefinition.class);
    assertThat(responseDef.getBase64Body(), is(nullValue()));
    assertThat(responseDef.getJsonBody(), is(nullValue()));
    assertThat(responseDef.getBody(), is("String content"));
  }

  private static final String JSON_BODY =
      "{	        								\n"
          + "		\"status\": 200,    				\n"
          + "		\"jsonBody\": {\"name\":\"wirmock\",\"isCool\":true} \n"
          + "}											";

  @Test
  public void correctlyUnmarshalsFromJsonWhenBodyIsJson() {
    ResponseDefinition responseDef = Json.read(JSON_BODY, ResponseDefinition.class);
    assertThat(responseDef.getBase64Body(), is(nullValue()));
    assertThat(responseDef.getBody(), is(nullValue()));

    JsonNode jsonNode = Json.node("{\"name\":\"wirmock\",\"isCool\":true}");
    assertThat(responseDef.getJsonBody(), is(jsonNode));
  }

  @Test
  public void correctlyMarshalsToJsonWhenBodyIsAString() throws Exception {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withBody("String content").build();

    JSONAssert.assertEquals(STRING_BODY, Json.write(responseDef), false);
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
    assertThat(responseDef.getBody(), is(nullValue()));
    assertThat(responseDef.getByteBody(), is(BODY));
  }

  @Test
  public void correctlyMarshalsToJsonWhenBodyIsBinary() throws Exception {
    ResponseDefinition responseDef =
        responseDefinition().withStatus(200).withBase64Body(BASE64_BODY).build();

    String actualJson = Json.write(responseDef);
    JSONAssert.assertEquals(actualJson, BINARY_BODY, false);
  }

  @Test
  public void indicatesBodyFileIfBodyContentIsNotAlsoSpecified() {
    ResponseDefinition responseDefinition = responseDefinition().withBodyFile("my-file").build();

    assertTrue(responseDefinition.specifiesBodyFile());
    assertFalse(responseDefinition.specifiesBodyContent());
  }

  @Test
  public void doesNotIndicateBodyFileIfBodyContentIsAlsoSpecified() {
    ResponseDefinition responseDefinition =
        responseDefinition().withBodyFile("my-file").withBody("hello").build();

    assertFalse(responseDefinition.specifiesBodyFile());
    assertTrue(responseDefinition.specifiesBodyContent());
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
    var responseDefinition =
        new ResponseDefinition(
            200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
    assertThat(responseDefinition.getHeaders(), notNullValue());
    assertThat(responseDefinition.getHeaders().size(), is(0));
  }

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
    var responseDefinition =
        new ResponseDefinition(
            200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
    assertThat(responseDefinition.getAdditionalProxyRequestHeaders(), notNullValue());
    assertThat(responseDefinition.getAdditionalProxyRequestHeaders().size(), is(0));
  }

  @Test
  public void removeProxyRequestHeadersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getRemoveProxyRequestHeaders(), empty());
    assertThrows(NullPointerException.class, () -> builder.setRemoveProxyRequestHeaders(null));
    assertThat(builder.getRemoveProxyRequestHeaders(), empty());
    assertThat(builder.build().getRemoveProxyRequestHeaders(), empty());
    var responseDefinition =
        new ResponseDefinition(
            200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
    assertThat(responseDefinition.getRemoveProxyRequestHeaders(), empty());
  }

  @Test
  public void transformersCannotBeNull() {
    var builder = new ResponseDefinition.Builder();
    assertThat(builder.getTransformers(), empty());
    assertThrows(NullPointerException.class, () -> builder.setTransformers(null));
    assertThat(builder.getTransformers(), notNullValue());
    assertThat(builder.getTransformers().size(), is(0));
    assertThat(builder.build().getTransformers(), empty());
    var responseDefinition =
        new ResponseDefinition(
            200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
    assertThat(responseDefinition.getTransformers(), empty());
  }

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
    var responseDefinition =
        new ResponseDefinition(
            200, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
    assertThat(responseDefinition.getTransformerParameters(), notNullValue());
    assertThat(responseDefinition.getTransformerParameters().size(), is(0));
  }

  @Test
  public void builderCopiesExistingValues() {
    var responseDefinition =
        new ResponseDefinition(
            200,
            "my status message",
            new Body("my body"),
            "my body file name",
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
            "https://browser.example.com",
            true);

    var copy = responseDefinition.toBuilder().build();
    assertThat(copy, is(responseDefinition));
    assertThat(copy.getStatus(), is(200));
    assertThat(copy.getStatusMessage(), is("my status message"));
    assertThat(copy.getBody(), is("my body"));
    assertThat(copy.getBodyFileName(), is("my body file name"));
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
    assertThat(copy.getProxyUrlPrefixToRemove(), is("my-prefix"));
    assertThat(copy.getFault(), is(Fault.EMPTY_RESPONSE));
    assertThat(copy.getTransformers(), is(List.of("my-transformer")));
    assertThat(copy.getTransformerParameters(), is(Parameters.one("p-1", "p1v1")));
    assertThat(copy.getBrowserProxyUrl(), is("https://browser.example.com"));
    assertThat(copy.wasConfigured(), is(true));
  }
}
