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

import static com.github.tomakehurst.wiremock.ContentPatternExtensionAcceptanceTest.MagicBytesPattern.magicBytes;
import static com.github.tomakehurst.wiremock.ContentPatternExtensionAcceptanceTest.StartsWithMatcher.startsWith;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.ContentPatternExtension;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.Arrays;
import java.util.List;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ContentPatternExtensionAcceptanceTest {

  private static final StubMapping CUSTOM =
      post(urlPathTemplate("/{path}"))
          .withQueryParam("query", startsWith("query"))
          .withFormParam("form", startsWith("form"))
          .withPathParam("path", startsWith("path"))
          .withHeader("header", startsWith("header"))
          .withCookie("cookie", startsWith("cookie"))
          .withRequestBody(startsWith("body"))
          .willReturn(ok())
          .build();

  @SuppressWarnings("unchecked")
  @RegisterExtension
  public static WireMockExtension wmLocal =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .extensions(StartsWithMatcherExtension.class)
                  .usingFilesUnderClasspath("content-pattern-extension"))
          .build();

  public static WireMock wmRemote;

  public static WireMockTestClient client;

  @SuppressWarnings("unchecked")
  @BeforeAll
  public static void beforeAll() {
    new WireMockServer(wireMockConfig().extensionScanningEnabled(true));
    wmRemote =
        create().port(wmLocal.getPort()).extensions(StartsWithMatcherExtension.class).build();
    client = new WireMockTestClient(wmLocal.getPort());
  }

  @Test
  public void localStubFor() {
    wmLocal.stubFor(
        post(urlPathTemplate("/stubFor/{path}"))
            .withQueryParam("query", startsWith("local"))
            .withQueryParam("query", equalTo("localQuery"))
            .withPathParam("path", startsWith("local"))
            .withPathParam("path", equalTo("localPath"))
            .withHeader("X-Local", startsWith("local"))
            .withHeader("X-Local", equalTo("localHeader"))
            .withRequestBody(startsWith("local"))
            .withRequestBody(equalTo("localBody"))
            .willReturn(ok()));
    assertThat(
        client
            .post(
                "/stubFor/localPath?query=localQuery",
                new StringEntity("localBody"),
                withHeader("X-Local", "localHeader"))
            .statusCode(),
        is(200));
  }

  @Test
  public void remoteRegister() {
    wmRemote.register(
        post(urlPathTemplate("/register/{path}"))
            .withQueryParam("query", startsWith("remote"))
            .withQueryParam("query", equalTo("remoteQuery"))
            .withPathParam("path", startsWith("remote"))
            .withPathParam("path", equalTo("remotePath"))
            .withHeader("X-Remote", startsWith("remote"))
            .withHeader("X-Remote", equalTo("remoteHeader"))
            .withRequestBody(startsWith("remote"))
            .withRequestBody(equalTo("remoteBody"))
            .willReturn(ok()));
    assertThat(
        client
            .post(
                "/register/remotePath?query=remoteQuery",
                new StringEntity("remoteBody"),
                withHeader("X-Remote", "remoteHeader"))
            .statusCode(),
        is(200));
  }

  @Test
  public void localGetStubMappings() {
    List<StubMapping> stubMappings = wmLocal.getStubMappings();
    assertThat(stubMappings.size(), is(1));
    assertThat(stubMappings.get(0).getRequest(), is(CUSTOM.getRequest()));
  }

  @Test
  public void remoteAllStubMappings() {
    List<StubMapping> stubMappings = wmRemote.allStubMappings().getMappings();
    assertThat(stubMappings.size(), is(1));
    assertThat(stubMappings.get(0).getRequest(), is(CUSTOM.getRequest()));
  }

  @Test
  public void clientMissingExtension() {
    WireMock wm = create().port(wmLocal.getPort()).build();
    InvalidInputException invalidInputException =
        Assertions.assertThrows(InvalidInputException.class, wm::allStubMappings);
    assertThat(invalidInputException.getErrors().getErrors().size(), is(2));
    Errors.Error first = invalidInputException.getErrors().first();
    assertThat(first.getCode(), is(10));
    assertThat(first.getSource().getPointer(), is("/mappings/0/request/headers/header"));
    assertThat(first.getTitle(), is("Error parsing JSON"));
    assertThat(
        first.getDetail(),
        is(
            "Could not resolve subtype of [simple type, class com.github.tomakehurst.wiremock.matching.StringValuePattern]: Cannot deduce unique subtype of `com.github.tomakehurst.wiremock.matching.StringValuePattern` (20 candidates match)"));
    Errors.Error second = invalidInputException.getErrors().getErrors().get(1);
    assertThat(second.getCode(), is(70));
    assertThat(second.getSource(), nullValue());
    assertThat(second.getTitle(), is("Extensions may not match between client and server."));
    assertThat(
        second.getDetail(),
        is(
            "Please ensure that the client has the same extensions by registering them via `WireMockBuilder#extensions()` and/or `WireMockBuilder#extensionScanningEnabled()`. For more information see https://wiremock.org/docs/extending-wiremock/."));
  }

  @Test
  public void serverMissingExtension() {
    MappingBuilder mapping =
        post("/missing").withRequestBody(magicBytes(new byte[] {})).willReturn(ok());
    InvalidInputException invalidInputException =
        Assertions.assertThrows(InvalidInputException.class, () -> wmRemote.register(mapping));
    assertThat(invalidInputException.getErrors().getErrors().size(), is(2));
    Errors.Error first = invalidInputException.getErrors().first();
    assertThat(first.getCode(), is(10));
    assertThat(first.getSource().getPointer(), is("/request/bodyPatterns/0"));
    assertThat(first.getTitle(), is("Error parsing JSON"));
    assertThat(
        first.getDetail(),
        is(
            "Could not resolve subtype of [simple type, class com.github.tomakehurst.wiremock.matching.ContentPattern<java.lang.Object>]: Cannot deduce unique subtype of `com.github.tomakehurst.wiremock.matching.ContentPattern<java.lang.Object>` (21 candidates match)"));
    Errors.Error second = invalidInputException.getErrors().getErrors().get(1);
    assertThat(second.getCode(), is(70));
    assertThat(second.getSource(), nullValue());
    assertThat(second.getTitle(), is("Extensions may not match between client and server."));
    assertThat(
        second.getDetail(),
        is(
            "Please ensure that the server has the same extensions by registering them with `--extensions` or via service loading. For more information see https://wiremock.org/docs/extending-wiremock/."));
  }

  @Test
  public void byteArrayPatternMatch() {
    byte[] magicBytes = new byte[] {0x50, 0x4B, 0x05, 0x06};
    wmLocal.stubFor(
        post(urlPathEqualTo("/byteArray"))
            .withRequestBody(magicBytes(magicBytes))
            .willReturn(ok()));
    assertThat(
        client.post("/byteArray", new StringEntity(new String(magicBytes))).statusCode(), is(200));
  }

  @Test
  public void byteArrayPatternDiff() {
    byte[] magicBytes = new byte[] {0x50, 0x4B, 0x05, 0x06};
    wmLocal.stubFor(
        post(urlPathEqualTo("/byteArray"))
            .withRequestBody(magicBytes(magicBytes))
            .willReturn(ok()));

    WireMockResponse response = client.post("/byteArray", new StringEntity("abc"));
    assertThat(response.statusCode(), is(404));
    assertThat(response.content(), containsString("[magicBytes]"));
    assertThat(response.content(), containsString("Body does not match"));
    assertThat(response.content(), containsString("[80, 75, 5, 6]"));
    assertThat(response.content(), containsString("[97, 98, 99]"));
  }

  @Test
  public void stringValuePatternMatch() {
    wmLocal.stubFor(
        post(urlPathEqualTo("/stringValue")).withRequestBody(startsWith("abc")).willReturn(ok()));
    assertThat(client.post("/stringValue", new StringEntity("abcd")).statusCode(), is(200));
  }

  @Test
  public void stringValuePatternDiff() {
    wmLocal.stubFor(
        post(urlPathEqualTo("/stringValue")).withRequestBody(startsWith("abc")).willReturn(ok()));

    WireMockResponse response = client.post("/stringValue", new StringEntity("def"));
    assertThat(response.statusCode(), is(404));
    assertThat(response.content(), containsString("[startsWith]"));
    assertThat(response.content(), containsString("Body does not match"));
    assertThat(response.content(), containsString("abc"));
    assertThat(response.content(), containsString("def"));
  }

  public static class StartsWithMatcherExtension implements ContentPatternExtension {

    @Override
    public Class<? extends ContentPattern<?>> getContentPatternClass() {
      return StartsWithMatcher.class;
    }

    @Override
    public String getName() {
      return "starts-with-matcher";
    }
  }

  public static class StartsWithMatcher extends StringValuePattern {

    public static StartsWithMatcher startsWith(String prefix) {
      return new StartsWithMatcher(prefix);
    }

    @JsonCreator
    public StartsWithMatcher(@JsonProperty("startsWith") String expectedValue) {
      super(expectedValue);
    }

    @Override
    public MatchResult match(String value) {
      if (value == null) {
        return MatchResult.noMatch();
      }
      return MatchResult.of(value.startsWith(expectedValue));
    }

    public String getStartsWith() {
      return expectedValue;
    }
  }

  public static class MagicBytesPattern extends ContentPattern<byte[]> {

    public static MagicBytesPattern magicBytes(byte[] magicBytes) {
      return new MagicBytesPattern(magicBytes);
    }

    @JsonCreator
    public MagicBytesPattern(@JsonProperty("format") byte[] magicBytes) {
      super(magicBytes);
    }

    @Override
    public String getName() {
      return "magicBytes";
    }

    @Override
    public String getExpected() {
      return Arrays.toString(expectedValue);
    }

    @Override
    public MatchResult match(byte[] value) {
      if (value.length >= expectedValue.length) {
        boolean matches = true;
        for (int i = 0; i < expectedValue.length; i++) {
          if (value[i] != expectedValue[i]) {
            matches = false;
            break;
          }
        }
        if (matches) {
          return MatchResult.exactMatch();
        }
      }
      return MatchResult.noMatch();
    }

    public byte[] getMagicBytes() {
      return expectedValue;
    }
  }
}
