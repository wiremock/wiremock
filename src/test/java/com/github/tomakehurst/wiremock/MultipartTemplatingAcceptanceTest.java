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
  public void multipartRequestPartsHeadersAreCaseInsensitive() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok(
                    "multipart:{{request.multipart}}\n"
                        + "text:content-type={{request.parts.text.headers.CoNtEnT-TyPe}}\n"
                        + "file:content-type={{request.parts.file.headers.cOnTeNt-tYpE}}")));

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
                + "text:content-type=text/plain; charset=ISO-8859-1\n"
                + "file:content-type=application/octet-stream"));
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

  @Test
  public void ableToReturnTheNumberOfParts() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok("multipart:{{request.multipart}}\n" + "part count = {{size request.parts}}")));
    WireMockResponse response =
        client.post(
            "/templated",
            MultipartEntityBuilder.create()
                .addTextBody("text", "hello", ContentType.TEXT_PLAIN)
                .addBinaryBody(
                    "file", "ABCD".getBytes(), ContentType.APPLICATION_OCTET_STREAM, "abcd.bin")
                .build());

    assertThat(response.content(), is("multipart:true\n" + "part count = 2"));
  }

  @Test
  public void ableToIterateOverParts() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok(
                    "multipart:{{request.multipart}}\n"
                        + "{{#each request.parts as |part|}}{{part.name}}:{{part.headers.content-type}}:{{part.body}}/\n{{/each}}")));
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
                + "file:application/octet-stream:ABCD/\n"
                + "text:text/plain; charset=ISO-8859-1:hello/\n"));
  }

  @Test
  public void requestPartTemplateModelCanBeOutputInATemplate() {
    wm.stubFor(
        post("/templated")
            .willReturn(
                ok(
                    "multipart:{{request.multipart}}\n"
                        + "{{#each request.parts as |part|}}{{part}}\n{{/each}}")));
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
                + "[name='file', headers={content-disposition=form-data; name=\"file\"; filename=\"abcd.bin\", content-type=application/octet-stream}, body=ABCD]\n"
                + "[name='text', headers={content-disposition=form-data; name=\"text\", content-type=text/plain; charset=ISO-8859-1}, body=hello]\n"));
  }
}
