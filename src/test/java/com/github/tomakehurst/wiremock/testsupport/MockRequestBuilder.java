/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.http.*;
import java.util.*;
import org.mockito.Mockito;

public class MockRequestBuilder {

  private String url = "/";
  private RequestMethod method = GET;
  private String clientIp = "x.x.x.x";
  private List<HttpHeader> individualHeaders = new ArrayList<>();
  private Map<String, Cookie> cookies = newHashMap();
  private List<QueryParameter> queryParameters = new ArrayList<>();

  private List<FormParameter> formParameters = new ArrayList<>();
  private String body = "";
  private String bodyAsBase64 = "";
  private Collection<Request.Part> multiparts = new ArrayList<>();
  private String protocol = "HTTP/1.1";

  private boolean browserProxyRequest = false;
  private String mockName;

  public MockRequestBuilder() {}

  public MockRequestBuilder(String mockName) {
    this.mockName = mockName;
  }

  public static MockRequestBuilder aRequest() {
    return new MockRequestBuilder();
  }

  public static MockRequestBuilder aRequest(String mockName) {
    return new MockRequestBuilder(mockName);
  }

  public MockRequestBuilder withUrl(String url) {
    this.url = url;
    return this;
  }

  public MockRequestBuilder withQueryParameter(String key, String... values) {
    queryParameters.add(new QueryParameter(key, Arrays.asList(values)));
    return this;
  }

  public MockRequestBuilder withFormParameter(String key, String... values) {
    formParameters.add(new FormParameter(key, Arrays.asList(values)));
    return this;
  }

  public MockRequestBuilder withMethod(RequestMethod method) {
    this.method = method;
    return this;
  }

  public MockRequestBuilder withClientIp(String clientIp) {
    this.clientIp = clientIp;
    return this;
  }

  public MockRequestBuilder withHeader(String key, String value) {
    individualHeaders.add(new HttpHeader(key, value));
    return this;
  }

  public MockRequestBuilder withCookie(String key, String value) {
    cookies.put(key, new Cookie(value));
    return this;
  }

  public MockRequestBuilder withBody(String body) {
    this.body = body;
    return this;
  }

  public MockRequestBuilder withBodyAsBase64(String bodyAsBase64) {
    this.bodyAsBase64 = bodyAsBase64;
    return this;
  }

  public MockRequestBuilder asBrowserProxyRequest() {
    this.browserProxyRequest = true;
    return this;
  }

  public MockRequestBuilder withMultiparts(Collection<Request.Part> parts) {
    this.multiparts = parts;
    return this;
  }

  public MockRequestBuilder withProtocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  public Request build() {
    final HttpHeaders headers = new HttpHeaders(individualHeaders);

    final Request request =
        mockName == null ? Mockito.mock(Request.class) : Mockito.mock(Request.class, mockName);
    when(request.getUrl()).thenReturn(url);
    when(request.getMethod()).thenReturn(method);
    when(request.getClientIp()).thenReturn(clientIp);
    for (HttpHeader header : headers.all()) {
      when(request.containsHeader(header.key())).thenReturn(true);
      when(request.getHeader(header.key())).thenReturn(header.firstValue());
    }

    for (HttpHeader header : headers.all()) {
      when(request.header(header.key())).thenReturn(header);
      if (header.key().equals(ContentTypeHeader.KEY) && header.isPresent()) {
        when(request.contentTypeHeader()).thenReturn(new ContentTypeHeader(header.firstValue()));
      }
    }

    for (QueryParameter queryParameter : queryParameters) {
      when(request.queryParameter(queryParameter.key())).thenReturn(queryParameter);
    }

    for (FormParameter formParameter : formParameters) {
      when(request.formParameter(formParameter.key())).thenReturn(formParameter);
    }

    when(request.header(Mockito.any(String.class))).thenReturn(httpHeader("key", "value"));

    when(request.getHeaders()).thenReturn(headers);
    when(request.getAllHeaderKeys()).thenReturn(new LinkedHashSet<>(headers.keys()));
    when(request.containsHeader(Mockito.any(String.class))).thenReturn(false);
    when(request.getCookies()).thenReturn(cookies);
    when(request.getBody()).thenReturn(body.getBytes());
    when(request.getBodyAsString()).thenReturn(body);
    when(request.getBodyAsBase64()).thenReturn(bodyAsBase64);
    when(request.getAbsoluteUrl()).thenReturn("http://localhost:8080" + url);
    when(request.isBrowserProxyRequest()).thenReturn(browserProxyRequest);
    when(request.isMultipart()).thenReturn(multiparts != null && !multiparts.isEmpty());
    when(request.getParts()).thenReturn(multiparts);
    when(request.getProtocol()).thenReturn(protocol);

    return request;
  }
}
