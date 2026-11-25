/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Stopwatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.jupiter.api.Test;

public class SnapshotStubMappingGeneratorTest {
  @Test
  public void apply() {
    final RequestPatternBuilder requestPatternBuilder = newRequestPattern().withUrl("/foo");
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition),
            true);

    StubMapping actual = stubMappingTransformer.apply(serveEvent());

    StubMapping expected =
        StubMapping.builder()
            .setRequest(requestPatternBuilder.build())
            .setResponse(responseDefinition)
            .setId(actual.getId())
            .setName("")
            .setPersistent(true)
            .build();

    assertThat(actual, is(expected));
  }

  @Test
  public void applyWithStrangePathAndCheckSanitizedState() {
    final RequestPatternBuilder requestPatternBuilder =
        newRequestPattern().withUrl("hello_1_2_3___ace--ace___and");
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition),
            false);

    StubMapping actual =
        stubMappingTransformer.apply(serveEventWithPath("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/and/¿?"));

    StubMapping expected =
        StubMapping.builder()
            .setRequest(requestPatternBuilder.build())
            .setResponse(responseDefinition)
            .setId(actual.getId())
            .setName("hello_1_2_3___ace--ace___and")
            .build();

    assertThat(actual, is(expected));
  }

  private static RequestPatternTransformer requestPatternTransformer(
      final RequestPatternBuilder requestPatternBuilder) {
    return new RequestPatternTransformer(null, null) {
      @Override
      public RequestPatternBuilder apply(Request request) {
        return requestPatternBuilder;
      }
    };
  }

  private static LoggedResponseDefinitionTransformer responseDefinitionTransformer(
      final ResponseDefinition responseDefinition) {
    return new LoggedResponseDefinitionTransformer() {
      @Override
      public ResponseDefinition apply(LoggedResponse response) {
        return responseDefinition;
      }
    };
  }

  @Test
  public void capturesServeTimeAsFixedDelayWhenEnabled() throws Exception {
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(newRequestPattern().withUrl("/foo")),
            responseDefinitionTransformer(responseDefinition),
            true,
            true);

    StubMapping actual = stubMappingTransformer.apply(serveEventWithServeTime());

    assertThat(actual.getResponse().getFixedDelayMilliseconds(), notNullValue());
    assertThat(actual.getResponse().getFixedDelayMilliseconds() >= 20, is(true));
  }

  @Test
  public void doesNotCaptureServeTimeWhenDisabled() throws Exception {
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(newRequestPattern().withUrl("/foo")),
            responseDefinitionTransformer(responseDefinition),
            true,
            false);

    StubMapping actual = stubMappingTransformer.apply(serveEventWithServeTime());

    assertThat(actual.getResponse().getFixedDelayMilliseconds(), nullValue());
  }

  @Test
  public void doesNotSetFixedDelayWhenServeEventIsUntimed() {
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(newRequestPattern().withUrl("/foo")),
            responseDefinitionTransformer(responseDefinition),
            true,
            true);

    StubMapping actual = stubMappingTransformer.apply(serveEvent());

    assertThat(actual.getResponse().getFixedDelayMilliseconds(), nullValue());
  }

  private static ServeEvent serveEventWithServeTime() throws Exception {
    Timing timing = Timing.create();
    Stopwatch stopwatch = Stopwatch.createStarted();
    Thread.sleep(30);
    timing.logProcessTime(stopwatch);
    timing.logResponseSendTime(stopwatch);

    return new ServeEvent(
        null,
        LoggedRequest.createFrom(aRequest().build()),
        null,
        null,
        LoggedResponse.from(Response.notConfigured(), UNLIMITED),
        false,
        timing,
        new LinkedBlockingDeque<>());
  }

  private static ServeEvent serveEventWithPath(String path) {
    return ServeEvent.of(LoggedRequest.createFrom(aRequest().withUrl(path).build()));
  }

  private static ServeEvent serveEvent() {
    return new ServeEvent(
        null,
        LoggedRequest.createFrom(aRequest().build()),
        null,
        null,
        LoggedResponse.from(Response.notConfigured(), UNLIMITED),
        false,
        Timing.UNTIMED,
        new LinkedBlockingDeque<>());
  }
}
