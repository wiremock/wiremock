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

import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs stateful post-processing tasks on stub mappings generated from ServeEvents:
 *
 * <ol>
 *   <li>Run any applicable StubMappingTransformers against the stub mappings.
 *   <li>Detect duplicate requests and either discard them or turn them into scenarios.
 *   <li>Extract response bodies to a separate file, if applicable.
 * </ol>
 */
class SnapshotStubMappingPostProcessor {
  private final boolean shouldRecordRepeatsAsScenarios;
  private final SnapshotStubMappingTransformerRunner transformerRunner;
  private final ResponseDefinitionBodyMatcher bodyExtractMatcher;
  private final SnapshotStubMappingBodyExtractor bodyExtractor;

  SnapshotStubMappingPostProcessor(
      boolean shouldRecordRepeatsAsScenarios,
      SnapshotStubMappingTransformerRunner transformerRunner,
      ResponseDefinitionBodyMatcher bodyExtractMatcher,
      SnapshotStubMappingBodyExtractor bodyExtractor) {
    this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
    this.transformerRunner = transformerRunner;
    this.bodyExtractMatcher = bodyExtractMatcher;
    this.bodyExtractor = bodyExtractor;
  }

  Pair<List<RecordError>, List<StubMapping>> process(
      List<Pair<ServeEvent, StubMapping>> serveEventsToStubMappings) {
    // 1. Run any applicable StubMappingTransformers against the stub mappings.
    List<StubMapping> transformedStubMappings = new ArrayList<>();
    List<RecordError> errors = new ArrayList<>();
    for (Pair<ServeEvent, StubMapping> serveEventToStubMapping : serveEventsToStubMappings) {
      StubGenerationResult result = transformerRunner.apply(serveEventToStubMapping);
      if (result instanceof StubGenerationResult.Success success) {
        transformedStubMappings.add(success.stubMapping());
      } else if (result instanceof StubGenerationResult.Failure failure) {
        errors.add(
            new RecordError.StubGenerationFailure(failure.reason(), serveEventToStubMapping.a));
      } else {
        throw new IllegalStateException("Unexpected result: " + result);
      }
    }

    // 2. Detect duplicate requests and either discard them or turn them into scenarios.
    Multiset<RequestPattern> requestCounts = HashMultiset.create();
    List<StubMapping> processedStubMappings = new ArrayList<>();
    for (StubMapping transformedStubMapping : transformedStubMappings) {
      requestCounts.add(transformedStubMapping.getRequest());

      // Skip duplicate requests if shouldRecordRepeatsAsScenarios is not enabled
      if (requestCounts.count(transformedStubMapping.getRequest()) > 1
          && !shouldRecordRepeatsAsScenarios) {
        continue;
      }

      processedStubMappings.add(transformedStubMapping);
    }

    if (shouldRecordRepeatsAsScenarios) {
      new ScenarioProcessor().putRepeatedRequestsInScenarios(processedStubMappings);
    }

    // 3. Extract response bodies to a separate file, if applicable.
    extractStubMappingBodies(processedStubMappings);

    return new Pair<>(errors, processedStubMappings);
  }

  private void extractStubMappingBodies(List<StubMapping> stubMappings) {
    if (bodyExtractMatcher == null) {
      return;
    }

    for (StubMapping stubMapping : stubMappings) {
      if (bodyExtractMatcher.match(stubMapping.getResponse()).isExactMatch()) {
        bodyExtractor.extractInPlace(stubMapping);
      }
    }
  }
}
