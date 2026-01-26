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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.url.PathParams;
import org.junit.jupiter.api.Test;

public class ImmutableRequestTest {

  @Test
  public void builderThrowsOnMissingAbsoluteUrl() {
    ImmutableRequest.Builder builder = ImmutableRequest.create().withMethod(RequestMethod.ANY);

    assertThrows(NullPointerException.class, builder::build);
  }

  @Test
  public void builderThrowsOnMissingMethod() {
    ImmutableRequest.Builder builder =
        ImmutableRequest.create().withAbsoluteUrl("https://example.com");

    assertThrows(NullPointerException.class, builder::build);
  }

  @Test
  public void builderCreatesMinimalRequest() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();

    assertThat(request, notNullValue());
  }

  @Test
  public void returnsNoCookiesByDefault() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.getCookies(), notNullValue());
    assertThat(request.getCookies(), anEmptyMap());
  }

  @Test
  public void returnsGivenMethod() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.getMethod(), is(RequestMethod.ANY));
  }

  @Test
  public void returnsGivenUrlPieces() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.getAbsoluteUrl(), is("https://example.com/"));
    assertThat(request.getScheme(), is("https"));
    assertThat(request.getHost(), is("example.com"));
    assertThat(request.getPort(), is(-1));
    assertThat(request.getUrl(), is("/"));
    assertThat(request.getPathParameters(), is(PathParams.empty()));
  }

  @Test
  public void returnsNoProtocolByDefault() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.getProtocol(), nullValue());
  }

  @Test
  public void returnsGivenProtocol() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .withProtocol("my-protocol")
            .build();
    assertThat(request.getProtocol(), is("my-protocol"));
  }

  @Test
  public void returnsNoBrowserProxyRequestByDefault() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.isBrowserProxyRequest(), is(false));
  }

  @Test
  public void returnsGivenBrowserProxyRequest() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .withBrowserProxyRequest(true)
            .build();
    assertThat(request.isBrowserProxyRequest(), is(true));
  }

  @Test
  public void returnsNoMultipartByDefault() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.isMultipart(), is(false));
  }

  @Test
  public void returnsGivenMultipart() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .withMultipart(true)
            .build();
    assertThat(request.isMultipart(), is(true));
  }

  @Test
  public void returnsNoClientIpByDefault() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .build();
    assertThat(request.getClientIp(), nullValue());
  }

  @Test
  public void returnsGivenClientIp() {
    ImmutableRequest request =
        ImmutableRequest.create()
            .withMethod(RequestMethod.ANY)
            .withAbsoluteUrl("https://example.com/")
            .withClientIp("client-ip")
            .build();
    assertThat(request.getClientIp(), is("client-ip"));
  }
}
