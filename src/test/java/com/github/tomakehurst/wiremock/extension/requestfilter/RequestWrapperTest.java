/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.requestfilter;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.matching.MockMultipart.mockPart;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class RequestWrapperTest {

  @Test
  public void changesTheRequestMethod() {
    MockRequest request = mockRequest().method(RequestMethod.GET);

    Request wrappedRequest = RequestWrapper.create().setMethod(RequestMethod.POST).wrap(request);

    assertThat(wrappedRequest.getMethod(), is(RequestMethod.POST));
  }

  @Test
  public void transformsTheUrl() {
    MockRequest request = mockRequest().url("/original-path?one=1&two=2");

    Request wrappedRequest =
        RequestWrapper.create()
            .transformAbsoluteUrl(
                existingUrl ->
                    existingUrl
                        .replace("my.domain", "wiremock.org")
                        .replace("/original-path", "/new-path"))
            .wrap(request);

    assertThat(wrappedRequest.getUrl(), is("/new-path?one=1&two=2"));
    assertThat(wrappedRequest.getAbsoluteUrl(), is("http://wiremock.org/new-path?one=1&two=2"));
  }

  @Test
  public void transformsAUrlWithNoPath() {
    MockRequest request = mockRequest().url("");

    Request wrappedRequest =
        RequestWrapper.create()
            .transformAbsoluteUrl(existingUrl -> existingUrl.replace("my.domain", "wiremock.org"))
            .wrap(request);

    assertThat(wrappedRequest.getUrl(), is(""));
    assertThat(wrappedRequest.getAbsoluteUrl(), is("http://wiremock.org"));
  }

  @Test
  public void addsSpecifiedHeaders() {
    MockRequest request = mockRequest().header("One", "1");

    Request wrappedRequest = RequestWrapper.create().addHeader("Two", "2").wrap(request);

    assertThat(wrappedRequest.getHeaders().size(), is(2));
    assertThat(wrappedRequest.getHeader("Two"), is("2"));
  }

  @Test
  public void removesSpecifiedHeaders() {
    MockRequest request = mockRequest().header("One", "1");

    Request wrappedRequest = RequestWrapper.create().removeHeader("One").wrap(request);

    assertThat(wrappedRequest.getHeaders().size(), is(0));
  }

  @Test
  public void transformsSpecifiedHeaders() {
    MockRequest request = mockRequest().header("One", "1").header("Two", "2", "3");

    Request wrappedRequest =
        RequestWrapper.create()
            .transformHeader(
                "One",
                headerValues ->
                    headerValues.stream()
                        .map(headerValue -> headerValue + "1")
                        .collect(Collectors.toList()))
            .transformHeader(
                "two",
                headerValues ->
                    headerValues.stream()
                        .map(headerValue -> headerValue + "2")
                        .collect(Collectors.toList()))
            .wrap(request);

    assertThat(wrappedRequest.getHeader("One"), is("11"));
    HttpHeader headerTwo = wrappedRequest.header("Two");
    assertThat(headerTwo.values().get(0), is("22"));
    assertThat(headerTwo.values().get(1), is("32"));
  }

  @Test
  public void containsHeaderChecksAreCaseInsensitive() {
    MockRequest request = mockRequest().header("One", "1");

    RequestWrapper wrappedRequest = new RequestWrapper(request);

    assertThat(wrappedRequest.containsHeader("one"), is(true));
    assertThat(wrappedRequest.containsHeader("onE"), is(true));
    assertThat(wrappedRequest.containsHeader("ONE"), is(true));
  }

  @Test
  public void addsSpecifiedCookies() {
    MockRequest request = mockRequest().cookie("One", "1");

    Request wrappedRequest =
        RequestWrapper.create()
            .addCookie("Two", new Cookie("2"))
            .addCookie("Three", new Cookie("3"))
            .wrap(request);

    assertThat(wrappedRequest.getCookies().size(), is(3));
    assertThat(wrappedRequest.getCookies().get("Two").firstValue(), is("2"));
    assertThat(wrappedRequest.getCookies().get("Three").firstValue(), is("3"));
  }

  @Test
  public void removesSpecifiedCookies() {
    MockRequest request = mockRequest().cookie("One", "1").cookie("Two", "2").cookie("Three", "3");

    Request wrappedRequest = RequestWrapper.create().removeCookie("Two").wrap(request);

    assertThat(wrappedRequest.getCookies().size(), is(2));
    assertThat(wrappedRequest.getCookies().get("Two"), nullValue());
  }

  @Test
  public void transformsSpecifiedCookies() {
    MockRequest request = mockRequest().cookie("One", "1");

    Request wrappedRequest =
        RequestWrapper.create()
            .transformCookie("One", cookie -> new Cookie(cookie.firstValue() + "1"))
            .wrap(request);

    assertThat(wrappedRequest.getCookies().get("One").firstValue(), is("11"));
  }

  @Test
  public void transformsAStringBody() {
    MockRequest request = mockRequest().body("One");

    Request wrappedRequest =
        RequestWrapper.create()
            .transformBody(
                existingBody -> {
                  String newValue = existingBody.asString().replace("One", "Two");
                  return new Body(newValue);
                })
            .wrap(request);

    assertThat(wrappedRequest.getBodyAsString(), is("Two"));
    assertThat(wrappedRequest.getBodyAsBase64(), is(encodeBase64("Two".getBytes())));
  }

  @Test
  public void transformsABinaryBody() {
    final byte[] initialBytes = new byte[] {1, 2, 3};
    final byte[] finalBytes = new byte[] {4, 5, 6};

    MockRequest request = mockRequest().body(initialBytes);

    Request wrappedRequest =
        RequestWrapper.create().transformBody(existingBody -> new Body(finalBytes)).wrap(request);

    assertThat(wrappedRequest.getBody(), is(finalBytes));
    assertThat(wrappedRequest.getBodyAsBase64(), is(encodeBase64(finalBytes)));
  }

  @Test
  public void transformsMultiparts() {
    MockRequest request =
        mockRequest()
            .part(mockPart().name("one").filename("text1.txt").body("1"))
            .part(mockPart().name("two").filename("text2.txt").body("2"));

    Request wrappedRequest =
        RequestWrapper.create()
            .transformParts(
                existingPart ->
                    existingPart.getName().equals("one")
                        ? mockPart().name("one").filename("text1.txt").body("1111")
                        : mockPart().name("two").filename("sample2.txt").body("2222"))
            .wrap(request);

    assertThat(wrappedRequest.getPart("one").getBody().asString(), is("1111"));
    assertThat(wrappedRequest.getPart("two").getBody().asString(), is("2222"));
    assertThat(
        wrappedRequest.getParts(),
        hasItem(mockPart().name("one").filename("text1.txt").body("1111")));
    assertThat(
        wrappedRequest.getParts(),
        hasItem(mockPart().name("two").filename("sample2.txt").body("2222")));
  }
}
