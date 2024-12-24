/*
 * Copyright (C) 2018-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.Strings.randomAlphanumeric;
import static com.github.tomakehurst.wiremock.testsupport.MultipartBody.part;
import static java.util.Collections.singletonList;
import static org.apache.hc.core5.http.ContentType.MULTIPART_FORM_DATA;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.apache.hc.client5.http.entity.mime.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
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

  /**
   * @see <a href="https://github.com/tomakehurst/wiremock/issues/1047">#1047</a>
   */
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

  /**
   * @see <a href="https://github.com/tomakehurst/wiremock/issues/1047">#1047</a>
   */
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

  @Test
  void acceptsAMultipartRequestWithCamelcasedContentTypeInformation() throws Exception {
    stubFor(
        post("/multipart-camelcased-content-type")
            .withMultipartRequestBody(aMultipart().withName("field1").withBody(containing("hello")))
            .withMultipartRequestBody(aMultipart().withName("field2").withBody(containing("world")))
            .willReturn(ok()));

    final URL url = new URL(wireMockServer.baseUrl() + "/multipart-camelcased-content-type");

    final String boundary = "uuid:" + UUID.randomUUID();

    // Test without leading Spaces
    HttpURLConnection connection = prepareUrlConnectionForCamelcasedContentTypeInformation(url);
    connection.setRequestProperty(
        "Content-Type", "Multipart/Form-Data; boundary=\"" + boundary + "\"");
    try (final OutputStream contentStream = connection.getOutputStream()) {
      contentStream.write(getRequestBodyForCamelcasedContentTypeInformationWithBoundary(boundary));
    }
    assertThat(connection.getResponseCode(), is(200));
  }

  @Test
  void multipartFieldsAreReturnedInTheServeEvents() throws Exception {
    stubFor(
        post("/multipart")
            .withMultipartRequestBody(
                aMultipart("foo-1")
                    .withHeader("Content-Type", equalTo("text/plain"))
                    .withBody(equalTo("Foo")))
            .withMultipartRequestBody(
                aMultipart("bar-1")
                    .withHeader("Content-Type", equalTo("text/plain"))
                    .withBody(equalTo("Bar")))
            .willReturn(ok()));

    final URL url = new URL(wireMockServer.baseUrl() + "/multipart");

    String multipartContent =
        "--foob\r\nContent-Disposition: form-data; name=\"foo-1\"\r\nContent-Type: text/plain\r\n\r\nFoo\r\n--foob\r\nContent-Disposition: form-data; name=\"bar-1\"\r\nContent-Type: text/plain\r\n\r\nBar\r\n--foob--";

    HttpURLConnection connection = prepareUrlConnectionForCamelcasedContentTypeInformation(url);
    connection.setRequestProperty("Content-Type", "Multipart/Form-Data; boundary=\"foob\"");
    try (final OutputStream contentStream = connection.getOutputStream()) {
      contentStream.write(multipartContent.getBytes());
    }
    assertThat(connection.getResponseCode(), is(200));

    List<ServeEvent> serveEvents = wm.getAllServeEvents();
    assertThat(serveEvents.size(), is(1));

    ServeEvent serveEvent = serveEvents.get(0);
    assertThat(serveEvent.getRequest().isMultipart(), is(true));
    assertThat(serveEvent.getRequest().getParts().size(), is(2));

    Collection<Request.Part> parts = serveEvent.getRequest().getParts();

    assertThat(
        parts.stream()
            .anyMatch(
                part -> part.getName().equals("foo-1") && part.getBody().asString().equals("Foo")),
        is(true));
    assertThat(
        parts.stream()
            .anyMatch(
                part -> part.getName().equals("bar-1") && part.getBody().asString().equals("Bar")),
        is(true));
  }

  private HttpURLConnection prepareUrlConnectionForCamelcasedContentTypeInformation(URL url)
      throws Exception {
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setUseCaches(false);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Accept", "*/*");
    return connection;
  }

  private byte[] getRequestBodyForCamelcasedContentTypeInformationWithBoundary(String boundary) {
    return ("--"
            + boundary
            + "\r\n"
            + "Content-Disposition: form-data; name=\"field1\"\r\n"
            + "\r\n"
            + "hello\r\n"
            + "--"
            + boundary
            + "\r\n"
            + "Content-Disposition: form-data; name=\"field2\"\r\n"
            + "\r\n"
            + "world\r\n"
            + "--"
            + boundary
            + "--")
        .getBytes();
  }

  @Test
  void acceptsAMultipartRequestWithCamelcasedContentTypeInformationPrefixedWithSpaces()
      throws Exception {
    stubFor(
        post("/multipart-camelcased-content-type")
            .withMultipartRequestBody(aMultipart().withName("field1").withBody(containing("hello")))
            .withMultipartRequestBody(aMultipart().withName("field2").withBody(containing("world")))
            .willReturn(ok()));

    final URL url = new URL(wireMockServer.baseUrl() + "/multipart-camelcased-content-type");

    final String boundary = "uuid:" + UUID.randomUUID();

    // Test without leading Spaces
    HttpURLConnection connection = prepareUrlConnectionForCamelcasedContentTypeInformation(url);
    connection.setRequestProperty(
        "Content-Type", "    Multipart/Form-Data; boundary=\"" + boundary + "\"");
    try (final OutputStream contentStream = connection.getOutputStream()) {
      contentStream.write(getRequestBodyForCamelcasedContentTypeInformationWithBoundary(boundary));
    }
    assertThat(connection.getResponseCode(), is(200));
  }

  @Test
  void acceptsAMultipartRelatedSOAPWithAttachmentRequest() throws Exception {
    final String soapBody =
        "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"
            + "  <soap:Header></soap:Header>\n"
            + "  <soap:Body>\n"
            + "    <ns1:Test xmlns:ns1=\"http://www.test.org/some-test-namespace\">\n"
            + "      <ns1:Attachment>\n"
            + "        <xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"ref-to-attachment%40some.domain.org\"/>\n"
            + "      </ns1:Attachment>\n"
            + "    </ns1:Test>\n"
            + "  </soap:Body>\n"
            + "</soap:Envelope>";

    stubFor(
        post("/multipart-related")
            .withMultipartRequestBody(
                aMultipart()
                    .withHeader(
                        "content-type",
                        equalTo("application/xop+xml; type=\"application/soap+xml\""))
                    .withBody(equalTo(soapBody)))
            .withMultipartRequestBody(
                aMultipart()
                    .withHeader("content-type", equalTo("text/plain"))
                    .withHeader("content-id", equalTo("<ref-to-attachment@some.domain.org>"))
                    .withBody(equalTo("some text/plain content")))
            .willReturn(ok()));

    final ClassicHttpRequest request =
        ClassicRequestBuilder.post(wireMockServer.baseUrl() + "/multipart-related")
            .setEntity(
                MultipartEntityBuilder.create()
                    .setMimeSubtype("related")
                    .addPart(
                        MultipartPartBuilder.create()
                            .setBody(
                                new StringBody(
                                    soapBody,
                                    ContentType.create("application/xop+xml")
                                        .withParameters(
                                            new BasicNameValuePair(
                                                "type", "application/soap+xml"))))
                            .build())
                    .addPart(
                        MultipartPartBuilder.create()
                            .setHeader("content-id", "<ref-to-attachment@some.domain.org>")
                            .setBody(
                                new StringBody(
                                    "some text/plain content", ContentType.create("text/plain")))
                            .build())
                    .build())
            .build();

    ClassicHttpResponse response = httpClient.execute(request);

    assertThat(response.getCode(), is(200));
  }
}
