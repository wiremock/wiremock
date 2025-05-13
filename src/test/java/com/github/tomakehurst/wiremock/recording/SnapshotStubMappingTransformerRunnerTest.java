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

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.GlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.NonGlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.StubMappingTransformerWithServeEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SnapshotStubMappingTransformerRunnerTest {
  private final StubMapping stubMapping = WireMock.get("/").build();
  private final ServeEvent serveEvent =
      ServeEvent.of(mockRequest().url("/whatever?some-query=my-value"));

  @Test
  public void applyWithNoTransformers() {
    StubGenerationResult result =
        new SnapshotStubMappingTransformerRunner(List.of())
            .apply(new Pair<>(serveEvent, stubMapping));

    assertEquals(new StubGenerationResult.Success(stubMapping), result);
  }

  @Test
  public void applyWithUnregisteredNonGlobalTransformer() {
    // Should not apply the transformer as it isn't registered
    StubGenerationResult result =
        new SnapshotStubMappingTransformerRunner(List.of(new NonGlobalStubMappingTransformer()))
            .apply(new Pair<>(serveEvent, stubMapping));

    assertEquals(new StubGenerationResult.Success(stubMapping), result);
  }

  @Test
  public void applyWithRegisteredNonGlobalTransformer() {
    StubGenerationResult result =
        new SnapshotStubMappingTransformerRunner(
                List.of(new NonGlobalStubMappingTransformer()),
                List.of("nonglobal-transformer"),
                null,
                null)
            .apply(new Pair<>(serveEvent, stubMapping));

    assertInstanceOf(StubGenerationResult.Success.class, result);
    StubMapping stubMapping = ((StubGenerationResult.Success) result).stubMapping();
    assertEquals("/?transformed=nonglobal", stubMapping.getRequest().getUrl());
  }

  @Test
  public void applyWithGlobalTransformer() {
    StubGenerationResult result =
        new SnapshotStubMappingTransformerRunner(List.of(new GlobalStubMappingTransformer()))
            .apply(new Pair<>(serveEvent, stubMapping));

    assertInstanceOf(StubGenerationResult.Success.class, result);
    StubMapping stubMapping = ((StubGenerationResult.Success) result).stubMapping();
    assertEquals("/?transformed=global", stubMapping.getRequest().getUrl());
  }

  @Test
  public void providesServeEventToTransformer() {
    StubGenerationResult result =
        new SnapshotStubMappingTransformerRunner(
                List.of(new StubMappingTransformerWithServeEvent()))
            .apply(new Pair<>(serveEvent, stubMapping));

    assertInstanceOf(StubGenerationResult.Success.class, result);
    StubMapping stubMapping = ((StubGenerationResult.Success) result).stubMapping();
    assertEquals("/?transformed=my-value", stubMapping.getRequest().getUrl());
  }
}
