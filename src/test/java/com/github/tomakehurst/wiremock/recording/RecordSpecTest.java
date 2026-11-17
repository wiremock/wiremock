/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RecordSpecTest {

  @Test
  void serialisesAllPropertiesWhenSet() {
    UUID id = UUID.fromString("5b2b6e2a-7f3c-4d2e-9a1b-0c8d7e6f5a4b");
    RecordSpec spec =
        new RecordSpecBuilder()
            .forTarget("http://example.com")
            .onlyRequestsMatching(getRequestedFor(urlPathMatching("/api/.*")))
            .onlyRequestIds(List.of(id))
            .allowNonProxied(true)
            .captureHeader("Accept")
            .captureHeader("Content-Type", true)
            .matchRequestBodyWithEqualToJson(true, false)
            .extractTextBodiesOver(2048)
            .extractBinaryBodiesOver(1024)
            .makeStubsPersistent(false)
            .ignoreRepeatRequests()
            .transformers("modify-response-header")
            .transformerParameters(Parameters.one("headerValue", "123"))
            .build();

    String json = Json.write(spec);

    // language=json
    String expected =
        """
        {
          "targetBaseUrl": "http://example.com",
          "filters": {
            "urlPathPattern": "/api/.*",
            "method": "GET",
            "ids": ["5b2b6e2a-7f3c-4d2e-9a1b-0c8d7e6f5a4b"],
            "allowNonProxied": true
          },
          "captureHeaders": {
            "Accept": {},
            "Content-Type": {
              "caseInsensitive": true
            }
          },
          "captureAllHeaders": false,
          "requestBodyPattern": {
            "matcher": "equalToJson",
            "ignoreArrayOrder": true,
            "ignoreExtraElements": false
          },
          "extractBodyCriteria": {
            "textSizeThreshold": "2048",
            "binarySizeThreshold": "1024"
          },
          "outputFormat": "FULL",
          "persist": false,
          "repeatsAsScenarios": false,
          "transformers": ["modify-response-header"],
          "transformerParameters": {
            "headerValue": "123"
          }
        }
        """;
    assertThat(json, jsonEquals(expected));
  }

  @Test
  void serialisesCaptureAllHeadersAndExcludeHeaders() {
    RecordSpec spec =
        new RecordSpecBuilder()
            .forTarget("http://example.com")
            .captureAllHeaders()
            .excludeHeaders("X-Request-Id", "X-Trace-Id")
            .build();

    String json = Json.write(spec);

    // language=json
    String expected =
        """
        {
          "targetBaseUrl": "http://example.com",
          "filters": {
            "allowNonProxied": false
          },
          "captureAllHeaders": true,
          "excludeHeaders": ["X-Request-Id", "X-Trace-Id"],
          "requestBodyPattern": {
            "matcher": "auto",
            "ignoreArrayOrder": true,
            "ignoreExtraElements": true,
            "caseInsensitive": false
          },
          "extractBodyCriteria": {
            "textSizeThreshold": "10240",
            "binarySizeThreshold": "0"
          },
          "outputFormat": "FULL",
          "persist": true,
          "repeatsAsScenarios": true
        }
        """;
    assertThat(json, jsonEquals(expected));
  }

  @Test
  void doesNotIncludeEmptyCollectionsWhenSerialising() {
    RecordSpec spec = new RecordSpecBuilder().forTarget("http://example.com").build();

    Map<String, Object> json = Json.read(Json.write(spec), Map.class);

    assertThat(json, not(hasKey("excludeHeaders")));
    assertThat(json, not(hasKey("captureHeaders")));
    assertThat(json, not(hasKey("transformers")));
    assertThat(json, not(hasKey("transformerParameters")));
  }

  @Test
  void roundTripsAllPropertiesWhenSet() {
    UUID id = UUID.fromString("5b2b6e2a-7f3c-4d2e-9a1b-0c8d7e6f5a4b");
    RecordSpec original =
        new RecordSpecBuilder()
            .forTarget("http://example.com")
            .onlyRequestsMatching(getRequestedFor(urlPathMatching("/api/.*")))
            .onlyRequestIds(List.of(id))
            .allowNonProxied(true)
            .captureHeader("Accept")
            .captureHeader("Content-Type", true)
            .matchRequestBodyWithEqualToJson(true, false)
            .extractTextBodiesOver(2048)
            .extractBinaryBodiesOver(1024)
            .makeStubsPersistent(false)
            .ignoreRepeatRequests()
            .transformers("modify-response-header")
            .transformerParameters(Parameters.one("headerValue", "123"))
            .build();

    RecordSpec roundTripped = Json.read(Json.write(original), RecordSpec.class);

    assertThat(roundTripped.getTargetBaseUrl(), is("http://example.com"));
    assertThat(
        roundTripped.getFilters().getFilters(),
        is(getRequestedFor(urlPathMatching("/api/.*")).build()));
    assertThat(roundTripped.getFilters().getIds(), contains(id));
    assertThat(roundTripped.getFilters().isAllowNonProxied(), is(true));
    assertThat(roundTripped.getCaptureHeaders().get("Accept").getCaseInsensitive(), nullValue());
    assertThat(roundTripped.getCaptureHeaders().get("Content-Type").getCaseInsensitive(), is(true));
    assertThat(roundTripped.getCaptureAllHeaders(), is(false));
    assertThat(roundTripped.getExcludeHeaders(), is(List.of()));
    assertThat(
        Json.write(roundTripped.getRequestBodyPatternFactory()),
        jsonEquals(Json.write(original.getRequestBodyPatternFactory())));
    assertThat(
        roundTripped.getExtractBodyCriteria(), is(new ResponseDefinitionBodyMatcher(2048, 1024)));
    assertThat(roundTripped.getOutputFormat(), is(SnapshotOutputFormatter.FULL));
    assertThat(roundTripped.shouldPersist(), is(false));
    assertThat(roundTripped.getRepeatsAsScenarios(), is(false));
    assertThat(roundTripped.getTransformers(), contains("modify-response-header"));
    assertThat(roundTripped.getTransformerParameters(), is(Parameters.one("headerValue", "123")));
  }

  @Test
  void roundTripsCaptureAllHeadersAndExcludeHeaders() {
    RecordSpec original =
        new RecordSpecBuilder()
            .forTarget("http://example.com")
            .captureAllHeaders()
            .excludeHeaders("X-Request-Id", "X-Trace-Id")
            .build();

    RecordSpec roundTripped = Json.read(Json.write(original), RecordSpec.class);

    assertThat(roundTripped.getCaptureAllHeaders(), is(true));
    assertThat(roundTripped.getExcludeHeaders(), contains("X-Request-Id", "X-Trace-Id"));
    assertThat(roundTripped.getCaptureHeaders(), nullValue());
  }
}
