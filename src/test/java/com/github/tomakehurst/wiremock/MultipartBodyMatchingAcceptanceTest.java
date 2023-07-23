/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.MultipartBody.part;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.hc.core5.http.ContentType.MULTIPART_FORM_DATA;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class MultipartBodyMatchingAcceptanceTest extends AcceptanceTestBase {

  CloseableHttpClient httpClient = HttpClientFactory.createClient();

  @Test
  public void acceptsAMultipartRequestContainingATextAndAFilePart() throws Exception {
    stubFor(
        post("/multipart")
            .withMultipartRequestBody(aMultipart().withName("text").withBody(containing("hello")))
            .withMultipartRequestBody(
                aMultipart().withName("file").withBody(binaryEqualTo("ABCD".getBytes())))
            .willReturn(ok()));

    ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/multipart")
            .setEntity(
                MultipartEntityBuilder.create()
                    .addTextBody("text", "hello")
                    .addBinaryBody("file", "ABCD".getBytes())
                    .build())
            .build();

    ClassicHttpResponse response = httpClient.execute(request);

    assertThat(EntityUtils.toString(response.getEntity()), response.getCode(), is(200));
  }

  @Test
  public void handlesAbsenceOfPartsInAMultipartRequest() throws Exception {
    stubFor(
        post("/empty-multipart")
            .withMultipartRequestBody(aMultipart().withName("bits").withBody(matching(".*")))
            .willReturn(ok()));

    ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/empty-multipart")
            .setHeader(
                "Content-Type",
                "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
            .setEntity(new StringEntity("", MULTIPART_FORM_DATA))
            .build();

    HttpResponse response = httpClient.execute(request);

    assertThat(response.getCode(), is(404));
  }

  /** @see <a href="https://github.com/tomakehurst/wiremock/issues/1047">#1047</a> */
  @Test
  public void acceptsAMultipartMixedRequestContainingATextAndAFilePart() throws Exception {
    stubFor(
        post("/multipart-mixed")
            .withMultipartRequestBody(aMultipart().withName("text").withBody(containing("hello")))
            .withMultipartRequestBody(
                aMultipart().withName("file").withBody(binaryEqualTo("ABCD".getBytes())))
            .willReturn(ok()));

    ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/multipart-mixed")
            .setEntity(
                MultipartEntityBuilder.create()
                    .setMimeSubtype("mixed")
                    .addTextBody("text", "hello")
                    .addBinaryBody("file", "ABCD".getBytes())
                    .build())
            .build();

    ClassicHttpResponse response = httpClient.execute(request);

    assertThat(EntityUtils.toString(response.getEntity()), response.getCode(), is(200));
  }

  /** @see <a href="https://github.com/tomakehurst/wiremock/issues/1047">#1047</a> */
  @Test
  public void acceptsAMultipartRelatedRequestContainingATextAndAFilePart() throws Exception {
    stubFor(
        post("/multipart-related")
            .withMultipartRequestBody(aMultipart().withName("text").withBody(containing("hello")))
            .withMultipartRequestBody(
                aMultipart().withName("file").withBody(binaryEqualTo("ABCD".getBytes())))
            .willReturn(ok()));

    ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/multipart-related")
            .setEntity(
                MultipartEntityBuilder.create()
                    .setMimeSubtype("related")
                    .addTextBody("text", "hello")
                    .addBinaryBody("file", "ABCD".getBytes())
                    .build())
            .build();

    ClassicHttpResponse response = httpClient.execute(request);

    assertThat(EntityUtils.toString(response.getEntity()), response.getCode(), is(200));
  }

  // https://github.com/tomakehurst/wiremock/issues/1179
  @Test
  public void multipartBodiesCanBeMatchedWhenStubsWithOtherBodyMatchTypesArePresent() {
    stubFor(
        post("/multipart")
            .withMultipartRequestBody(
                aMultipart().withHeader("Content-Disposition", containing("wiremocktest")))
            .willReturn(ok()));

    stubFor(post("/json").withRequestBody(equalToJson("{ \"stuff\": 123 }")).willReturn(ok()));

    WireMockResponse response =
        testClient.postWithMultiparts(
            "/multipart", singletonList(part("wiremocktest", "Whatever", TEXT_PLAIN)));

    assertThat(response.statusCode(), is(200));
  }

  @Test
  @Timeout(2)
  void handlesLargeMultipartBody() {
    stubFor(
        post("/multipart")
            .withMultipartRequestBody(
                aMultipart().withHeader("Content-Disposition", containing("vlarge")))
            .willReturn(ok()));

    WireMockResponse response =
        testClient.postWithMultiparts(
            "/multipart", singletonList(part("vlarge", randomAlphanumeric(300000), TEXT_PLAIN)));

    assertThat(response.statusCode(), is(200));
  }
}
