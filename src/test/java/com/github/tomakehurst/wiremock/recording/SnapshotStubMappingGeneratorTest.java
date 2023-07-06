/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.Test;

class SnapshotStubMappingGeneratorTest {
  @Test
  void apply() {
    final RequestPatternBuilder requestPatternBuilder = newRequestPattern().withUrl("/foo");
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition));

    StubMapping actual = stubMappingTransformer.apply(serveEvent());
    StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
    expected.setId(actual.getId());

    assertThat(actual, is(expected));
  }

  @Test
  void applyWithStrangePathAndCheckSanitizedState() {
    final RequestPatternBuilder requestPatternBuilder =
        newRequestPattern().withUrl("hello_1_2_3___ace--ace___and");
    final ResponseDefinition responseDefinition = ResponseDefinition.ok();

    SnapshotStubMappingGenerator stubMappingTransformer =
        new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition));

    StubMapping actual =
        stubMappingTransformer.apply(serveEventWithPath("/hello/1/2/3__!/ẮČĖ--ace/¥$$/$/and/¿?"));
    StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
    expected.setId(actual.getId());

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
        new LinkedBlockingQueue<>());
  }
}
