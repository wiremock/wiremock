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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.GlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.NonGlobalStubMappingTransformer;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
public class SnapshotStubMappingTransformerRunnerTest {
  private final StubMapping stubMapping = WireMock.get("/").build();

  @Test
  public void applyWithNoTransformers() {
    StubMapping result = new SnapshotStubMappingTransformerRunner(List.of()).apply(stubMapping);

    assertEquals(stubMapping, result);
  }

  @Test
  public void applyWithUnregisteredNonGlobalTransformer() {
    // Should not apply the transformer as it isn't registered
    StubMapping result =
        new SnapshotStubMappingTransformerRunner(List.of(new NonGlobalStubMappingTransformer()))
            .apply(stubMapping);

    assertEquals(stubMapping, result);
  }

  @Test
  public void applyWithRegisteredNonGlobalTransformer() {
    StubMapping result =
        new SnapshotStubMappingTransformerRunner(
                List.of(new NonGlobalStubMappingTransformer()),
                List.of("nonglobal-transformer"),
                null,
                null)
            .apply(stubMapping);

    assertEquals("/?transformed=nonglobal", result.getRequest().getUrl());
  }

  @Test
  public void applyWithGlobalTransformer() {
    StubMapping result =
        new SnapshotStubMappingTransformerRunner(List.of(new GlobalStubMappingTransformer()))
            .apply(stubMapping);

    assertEquals("/?transformed=global", result.getRequest().getUrl());
  }
}
