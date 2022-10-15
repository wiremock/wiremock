/*
 * Copyright (C) 2017-2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.Compression.DEFLATE;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.common.Strings.DEFAULT_CHARSET;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.common.Compression;
import com.github.tomakehurst.wiremock.http.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class LoggedResponseDefinitionTransformerTest {
  private LoggedResponseDefinitionTransformer aTransformer() {
    return new LoggedResponseDefinitionTransformer();
  }

  @Test
  public void applyWithEmptyHeadersAndBody() {
    final LoggedResponse response =
        LoggedResponse.from(Response.response().status(401).build(), UNLIMITED);
    assertEquals(responseDefinition().withStatus(401).build(), aTransformer().apply(response));
  }

  @Test
  public void applyWithTextBody() {
    final LoggedResponse response =
        LoggedResponse.from(
            Response.response()
                .headers(new HttpHeaders(new ContentTypeHeader("text/plain")))
                .body("foo")
                .build(),
            UNLIMITED);
    final ResponseDefinition expected =
        responseDefinition().withHeader("Content-Type", "text/plain").withBody("foo").build();
    assertEquals(expected, aTransformer().apply(response));
  }

  @Test
  public void applyWithBinaryBody() {
    final byte[] body = new byte[] {0x1, 0xc, 0x3, 0xb, 0x1};
    final LoggedResponse response =
        LoggedResponse.from(
            Response.response()
                .headers(new HttpHeaders(new ContentTypeHeader("application/octet-stream")))
                .body(body)
                .build(),
            UNLIMITED);
    final ResponseDefinition expected =
        responseDefinition()
            .withHeader("Content-Type", "application/octet-stream")
            .withBody(body)
            .build();
    assertEquals(expected, aTransformer().apply(response));
  }

  @Test
  public void preservesHeadersExceptThoseSpecificallyExcluded() {
    final String uncompressed = "Giant response body... so we'll compress it";
    final byte[] compressedData = DEFLATE.compress(uncompressed);
    final LoggedResponse response =
        LoggedResponse.from(
            Response.response()
                .headers(
                    new HttpHeaders(
                        httpHeader("Content-Encoding", DEFLATE.contentEncodingValue), // Excluded
                        httpHeader("content-LENGTH", Integer.toString(compressedData.length)), // Excluded
                        httpHeader("transfer-encoding", "chunked"), // Excluded
                        httpHeader("Accept", "application/json"),
                        httpHeader("X-foo", "Bar")))
                    .body(compressedData)
                .build(),
            UNLIMITED);
    final ResponseDefinition expected =
        responseDefinition()
            .withHeader("Accept", "application/json")
            .withHeader("X-foo", "Bar")
            .withBase64Body(encodeBase64(uncompressed.getBytes(DEFAULT_CHARSET)))
            .build();
    assertEquals(expected, aTransformer().apply(response));
  }

  @Test
  public void transformsWhenNoHeadersArePresent() {
    final byte[] body = new byte[] {0x1, 0xc, 0x3, 0xb, 0x1};
    final LoggedResponse response =
        LoggedResponse.from(
            Response.response().status(500).body(body).headers(null).build(), UNLIMITED);

    final ResponseDefinition expected = responseDefinition().withStatus(500).withBody(body).build();
    assertEquals(expected, aTransformer().apply(response));
  }
}
