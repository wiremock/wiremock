/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MultipartTemplatingAcceptanceTest {

  WireMockTestClient client;

  @RegisterExtension
  public static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(options().dynamicPort().templatingEnabled(true).globalTemplating(true))
          .build();

  @BeforeEach
  void init() {
    client = new WireMockTestClient(wm.getPort());
  }

  @Test
  public void multipartRequestPartsAreAvailableViaTemplating() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok(
                    "multipart:{{request.multipart}}\n"
                        + "text:binary={{request.parts.text.binary}}:{{request.parts.text.headers.content-type}}:{{request.parts.text.body}}\n"
                        + "file:binary={{request.parts.file.binary}}:{{request.parts.file.headers.content-type}}:{{request.parts.file.bodyAsBase64}}")));

    WireMockResponse response =
        client.post(
            "/templated",
            MultipartEntityBuilder.create()
                .addTextBody("text", "hello", ContentType.TEXT_PLAIN)
                .addBinaryBody(
                    "file", "ABCD".getBytes(), ContentType.APPLICATION_OCTET_STREAM, "abcd.bin")
                .build());

    assertThat(
        response.content(),
        is(
            "multipart:true\n"
                + "text:binary=false:text/plain; charset=ISO-8859-1:hello\n"
                + "file:binary=true:application/octet-stream:QUJDRA=="));
  }

  @Test
  public void returnsEmptyPartsInTemplateWhenRequestIsNotMultipart() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok(
                    "multipart:{{request.multipart}}\n"
                        + "text:{{request.parts.text.headers.content-type}}:{{request.parts.text.body}}")));

    WireMockResponse response = client.postJson("/templated", "{}");

    assertThat(response.content(), is("multipart:false\n" + "text::"));
  }

  // TODO list parts and/or get the count

  // TODO add bodyAsBase64 to main request body template model for consistency

  // TODO case-insensitive map for headers or normalisation of key case?

}
