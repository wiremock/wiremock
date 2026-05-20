/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.Strings.isNullOrEmpty;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.hc.core5.http.ContentType.DEFAULT_BINARY;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.MimeTypes;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;

@SuppressWarnings("HttpUrlsUsage")
public class WireMockTestClient {

  private static final String LOCAL_WIREMOCK_ROOT = "http://%s:%d%s";
  private static final String LOCAL_WIREMOCK_SNAPSHOT_PATH = "/__admin/recordings/snapshot";

  private final int port;
  private final String address;

  private final CloseableHttpClient client;

  public WireMockTestClient(int port, String address) {
    this.port = port;
    this.address = address;

    this.client = httpClient();
  }

  public WireMockTestClient(int port) {
    this(port, "localhost");
  }

  public WireMockTestClient() {
    this(8080);
  }

  private String mockServiceUrlFor(String path) {
    return String.format(LOCAL_WIREMOCK_ROOT, address, port, path);
  }

  public WireMockResponse get(String url, TestHttpHeader... headers) {
    return execute(RequestMethod.GET, url, null, null, null, headers);
  }

  public WireMockResponse head(String url, TestHttpHeader... headers) {
    return execute(RequestMethod.HEAD, url, null, null, null, headers);
  }

  public WireMockResponse getWithBody(
      String url, String body, String contentType, TestHttpHeader... headers) {
    return execute(RequestMethod.GET, url, body, contentType, null, headers);
  }

  public WireMockResponse getViaProxy(String url) {
    return getViaProxy(url, port);
  }

  public WireMockResponse getViaProxy(String url, int proxyPort) {
    return getViaProxy(url, proxyPort, HttpHost.DEFAULT_SCHEME.getId());
  }

  public WireMockResponse getViaProxy(String url, int proxyPort, String scheme) {
    URI targetUri = URI.create(url);
    HttpHost proxy = new HttpHost(scheme, address, proxyPort);

    HttpHost target = new HttpHost(targetUri.getScheme(), targetUri.getHost(), targetUri.getPort());
    HttpGet req =
        new HttpGet(
            targetUri.getPath()
                + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));
    req.removeHeaders("Host");

    System.out.println("executing request to " + targetUri + "(" + target + ") via " + proxy);

    try (CloseableHttpClient httpClientUsingProxy =
        HttpClientBuilder.create()
            .disableAuthCaching()
            .disableAutomaticRetries()
            .disableCookieManagement()
            .disableRedirectHandling()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(buildTrustWireMockDefaultCertificateSSLContext())
                            .setHostnameVerifier(new NoopHostnameVerifier())
                            .build())
                    .build())
            .setProxy(proxy)
            .build()) {

      try (CloseableHttpResponse httpResponse = httpClientUsingProxy.execute(target, req)) {
        return new WireMockResponse(httpResponse);
      }
    } catch (IOException ioe) {
      return Exceptions.throwUnchecked(ioe, WireMockResponse.class);
    }
  }

  public WireMockResponse put(String url, TestHttpHeader... headers) {
    return execute(RequestMethod.PUT, url, null, null, null, headers);
  }

  public WireMockResponse putWithBody(
      String url, String body, String contentType, TestHttpHeader... headers) {
    return execute(RequestMethod.PUT, url, body, contentType, null, headers);
  }

  public WireMockResponse patchWithBody(
      String url, String body, String contentType, TestHttpHeader... headers) {
    return execute(RequestMethod.PATCH, url, body, contentType, null, headers);
  }

  public WireMockResponse post(String url, TestHttpHeader... headers) {
    return execute(RequestMethod.POST, url, null, null, null, headers);
  }

  public WireMockResponse postWithBody(String url, String body, TestHttpHeader... headers) {
    return execute(RequestMethod.POST, url, body, null, null, headers);
  }

  public WireMockResponse query(String url, TestHttpHeader... headers) {
    return execute(QUERY, url, null, null, null, headers);
  }

  public WireMockResponse queryWithBody(
      String url, String body, String contentType, TestHttpHeader... headers) {
    return execute(QUERY, url, body, contentType, null, headers);
  }

  public WireMockResponse queryXml(String url, String body, TestHttpHeader... headers) {
    return queryWithBody(url, body, "application/xml", headers);
  }

  @SuppressWarnings("UnusedReturnValue")
  public WireMockResponse queryJson(String url, String body, TestHttpHeader... headers) {
    return queryWithBody(url, body, "application/json", headers);
  }

  public WireMockResponse queryWithMultiparts(
      String url, Collection<MultipartBody> parts, TestHttpHeader... headers) {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    if (parts != null) {
      for (MultipartBody part : parts) {
        builder.addPart(part.getFilename(), part);
      }
    }

    HttpEntity entity = builder.build();
    HttpUriRequest httpQuery =
        new HttpUriRequestBase(QUERY.toString(), URI.create(mockServiceUrlFor(url)));
    httpQuery.setEntity(entity);
    return executeMethodAndConvertExceptions(httpQuery, headers);
  }

  public WireMockResponse postWithBody(
      String url, String body, String bodyMimeType, TestHttpHeader... headers) {
    return execute(RequestMethod.POST, url, body, bodyMimeType, null, headers);
  }

  public WireMockResponse postWithBody(
      String url,
      String body,
      String bodyMimeType,
      String bodyEncoding,
      TestHttpHeader... headers) {
    return execute(RequestMethod.POST, url, body, bodyMimeType, bodyEncoding, headers);
  }

  public WireMockResponse postWithMultiparts(
      String url, Collection<MultipartBody> parts, TestHttpHeader... headers) {
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    if (parts != null) {
      for (MultipartBody part : parts) {
        builder.addPart(part.getFilename(), part);
      }
    }

    return post(url, builder.build(), headers);
  }

  @SuppressWarnings("UnusedReturnValue")
  public WireMockResponse postWithChunkedBody(String url, byte[] body) {
    return post(url, new InputStreamEntity(new ByteArrayInputStream(body), -1, DEFAULT_BINARY));
  }

  // TODO break dependency on `HttpEntity` so we are decoupled from Apache HTTP Client in the API
  public WireMockResponse post(String url, HttpEntity entity, TestHttpHeader... headers) {
    HttpPost httpPost = new HttpPost(mockServiceUrlFor(url));
    httpPost.setEntity(entity);
    return executeMethodAndConvertExceptions(httpPost, headers);
  }

  public WireMockResponse postJson(String url, String body, TestHttpHeader... headers) {
    return postWithBody(url, body, headers);
  }

  public WireMockResponse putJson(String url, String body, TestHttpHeader... headers) {
    return putWithBody(url, body, MimeTypes.JSON.toString(), headers);
  }

  public WireMockResponse postXml(String url, String body, TestHttpHeader... headers) {
    return execute(POST, url, body, "application/xml", null, headers);
  }

  @SuppressWarnings("UnusedReturnValue")
  public WireMockResponse patchWithBody(
      String url, String body, String bodyMimeType, String bodyEncoding) {
    return execute(PATCH, url, body, bodyMimeType, bodyEncoding);
  }

  public WireMockResponse delete(String url) {
    return execute(DELETE, url, null, null, null);
  }

  public WireMockResponse deleteWithBody(
      String url, String body, String contentType, TestHttpHeader... headers) {
    return execute(DELETE, url, body, contentType, null, headers);
  }

  public WireMockResponse options(String url, TestHttpHeader... headers) {
    return execute(OPTIONS, url, null, null, null, headers);
  }

  public void addResponse(String responseSpecJson) {
    addResponse(responseSpecJson, "UTF-8");
  }

  public void addResponse(String responseSpecJson, String charset) {
    int status =
        postWithBody("/__admin/mappings", responseSpecJson, MimeTypes.JSON.toString(), charset)
            .statusCode();
    if (status != HTTP_CREATED) {
      throw new RuntimeException("Returned status code was " + status);
    }
  }

  public void editMapping(String mappingSpecJson) {
    StubMapping stubMapping = Json.read(mappingSpecJson, StubMapping.class);
    WireMockResponse wireMockResponse =
        putJson("/__admin/mappings/" + stubMapping.getId(), mappingSpecJson);
    int status = wireMockResponse.statusCode();
    if (status != HTTP_OK) {
      throw new RuntimeException("Returned status code was " + status);
    }
  }

  public void resetDefaultMappings() {
    int status = post("/__admin/mappings/reset").statusCode();
    if (status != HTTP_OK) {
      throw new RuntimeException("Returned status code was " + status);
    }
  }

  public String snapshot(String snapshotSpecJson) {
    WireMockResponse response = postJson(LOCAL_WIREMOCK_SNAPSHOT_PATH, snapshotSpecJson);
    if (response.statusCode() != HTTP_OK) {
      throw new RuntimeException("Returned status code was " + response.statusCode());
    }
    return response.content();
  }

  private WireMockResponse execute(
      RequestMethod method,
      String url,
      String body,
      String contentType,
      String charset,
      TestHttpHeader... headers) {
    return execute(method.getName(), url, body, contentType, charset, headers);
  }

  private WireMockResponse execute(
      String method,
      String url,
      String body,
      String contentType,
      String charset,
      TestHttpHeader... headers) {
    String actualUrl = URI.create(url).isAbsolute() ? url : mockServiceUrlFor(url);
    ClassicHttpRequest httpRequest = new HttpUriRequestBase(method, URI.create(actualUrl));
    if (body != null) {
      ContentType type =
          contentType != null ? ContentType.create(contentType) : ContentType.APPLICATION_JSON;
      Charset charsetEnc = charset == null ? UTF_8 : Charset.forName(charset);
      StringEntity entity = new StringEntity(body, type.withCharset(charsetEnc));
      httpRequest.setEntity(entity);
    }
    return executeMethodAndConvertExceptions(httpRequest, headers);
  }

  private WireMockResponse executeMethodAndConvertExceptions(
      ClassicHttpRequest httpRequest, TestHttpHeader... headers) {
    for (TestHttpHeader header : headers) {
      httpRequest.addHeader(header.getName(), header.getValue());
    }
    try (CloseableHttpResponse httpResponse = client.execute(httpRequest)) {
      return new WireMockResponse(httpResponse);
    } catch (IOException ioe) {
      return throwUnchecked(ioe, null);
    }
  }

  public WireMockResponse getWithPreemptiveCredentials(
      String url, int port, String username, String password) {

    BasicScheme basicAuth = new BasicScheme();
    basicAuth.initPreemptive(new UsernamePasswordCredentials(username, password.toCharArray()));

    HttpClientContext localContext = HttpClientContext.create();
    HttpHost target = new HttpHost("localhost", port);
    localContext.resetAuthExchange(target, basicAuth);

    HttpGet httpget = new HttpGet(url);
    try (CloseableHttpResponse response = client.execute(target, httpget, localContext)) {
      return new WireMockResponse(response);
    } catch (IOException e) {
      return throwUnchecked(e, null);
    }
  }

  public WireMockResponse request(final String methodName, String url, TestHttpHeader... headers) {
    return execute(methodName, url, null, null, null, headers);
  }

  public WireMockResponse request(
      final String methodName, String url, String body, TestHttpHeader... headers) {
    return execute(methodName, url, body, null, null, headers);
  }

  private static CloseableHttpClient httpClient() {
    return HttpClientBuilder.create()
        .setUserAgent("WireMock Test Client")
        .disableAuthCaching()
        .disableAutomaticRetries()
        .disableCookieManagement()
        .disableRedirectHandling()
        .disableContentCompression()
        .setConnectionManager(
            PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(1000)
                .setMaxConnTotal(1000)
                .setConnectionFactory(
                    new ManagedHttpClientConnectionFactory(
                        null, CharCodingConfig.custom().setCharset(UTF_8).build(), null))
                .build())
        .build();
  }

  private static SSLContext buildTrustWireMockDefaultCertificateSSLContext() {
    try {
      return SSLContexts.custom()
          .loadTrustMaterial(
              null,
              (chain, authType) ->
                  chain[0].getSubjectDN().getName().startsWith("CN=Tom Akehurst")
                      || chain[0]
                          .getSubjectDN()
                          .getName()
                          .equals("CN=WireMock Local Self Signed Root Certificate")
                      || chain.length == 2
                          && chain[1]
                              .getSubjectDN()
                              .getName()
                              .equals("CN=WireMock Local Self Signed Root Certificate"))
          .build();
    } catch (Exception e) {
      return throwUnchecked(e, SSLContext.class);
    }
  }
}
