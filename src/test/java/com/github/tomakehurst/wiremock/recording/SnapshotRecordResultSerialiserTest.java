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

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;
import java.util.UUID;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;
import org.junit.jupiter.api.Test;

public class SnapshotRecordResultSerialiserTest {

  @Test
  public void supportsFullResponseWithErrors() {
    SnapshotRecordResult result =
        new SnapshotRecordResult.Full(
            List.of(get("/hello").willReturn(created()).build()),
            List.of(
                new RecordError.StubGenerationFailure(
                    "bad request",
                    ServeEvent.of(mockRequest().method(GET).url("/hello"))
                        .withResponseDefinition(ok().build()))));
    String json = Json.write(result);

    String expected =
        """
            {
              "errors": [
                {
                  "errorType": "stub-generation-failure",
                  "reason": "bad request",
                  "originalServeEvent": {
                    "request": {
                      "method": "GET"
                    }
                  }
                }
              ],
              "mappings": [
                {
                  "request": {
                    "url": "/hello",
                    "method": "GET"
                  },
                  "response": {
                    "status": 201
                  }
                }
              ]
            }
        """;
    assertThat(json, jsonEquals(expected).withOptions(new Options(Option.IGNORING_EXTRA_FIELDS)));
  }

  @Test
  public void supportsIdsOnlyResponseWithErrors() {
    SnapshotRecordResult result =
        new SnapshotRecordResult.Ids(
            List.of(
                UUID.fromString("d3f32721-ab5e-479c-9f7a-fda76ed5d803"),
                UUID.fromString("162d0567-4baf-408b-ad7f-41a779638082"),
                UUID.fromString("02ee46c3-0b49-40ca-a424-8298c099b6db")),
            List.of(
                new RecordError.StubGenerationFailure(
                    "bad request",
                    ServeEvent.of(mockRequest().method(GET).url("/hello"))
                        .withResponseDefinition(ok().build()))));
    String json = Json.write(result);

    String expected =
        """
            {
              "errors": [
                {
                  "errorType": "stub-generation-failure",
                  "reason": "bad request",
                  "originalServeEvent": {
                    "request": {
                      "method": "GET"
                    }
                  }
                }
              ],
              "ids": [
                "d3f32721-ab5e-479c-9f7a-fda76ed5d803",
                "162d0567-4baf-408b-ad7f-41a779638082",
                "02ee46c3-0b49-40ca-a424-8298c099b6db"
              ]
            }
        """;
    assertThat(json, jsonEquals(expected).withOptions(new Options(Option.IGNORING_EXTRA_FIELDS)));
  }
}
