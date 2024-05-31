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

import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.indexOf;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.ServerExtensions;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.RecorderStateStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Recorder {

  private final Admin admin;
  private final ServerExtensions extensions;
  private final BlobStore filesBlobStore;

  private final RecorderStateStore stateStore;

  public Recorder(
          Admin admin, ServerExtensions extensions, BlobStore filesBlobStore, RecorderStateStore stateStore) {
    this.admin = admin;
    this.extensions = extensions;
    this.filesBlobStore = filesBlobStore;
    this.stateStore = stateStore;
  }

  public synchronized void startRecording(RecordSpec spec) {
    RecorderState state = stateStore.get();
    if (state.getStatus() == RecordingStatus.Recording) {
      return;
    }

    StubMapping proxyMapping = null;
    if (spec.getTargetBaseUrl() != null && !spec.getTargetBaseUrl().isEmpty()) {
      proxyMapping = proxyAllTo(spec.getTargetBaseUrl()).build();
      admin.addStubMapping(proxyMapping);
    }

    List<ServeEvent> serveEvents = admin.getServeEvents().getServeEvents();
    UUID initialId = serveEvents.isEmpty() ? null : serveEvents.get(0).getId();
    state = state.start(initialId, proxyMapping, spec);
    stateStore.set(state);

    notifier().info("Started recording with record spec:\n" + Json.write(spec));
  }

  public synchronized SnapshotRecordResult stopRecording() {
    RecorderState state = stateStore.get();
    if (state.getStatus() != RecordingStatus.Recording) {
      throw new NotRecordingException();
    }

    List<ServeEvent> serveEvents = admin.getServeEvents().getServeEvents();

    UUID lastId = serveEvents.isEmpty() ? null : serveEvents.get(0).getId();
    state = state.stop(lastId);
    stateStore.set(state);

    if (state.getProxyMapping() != null) {
      admin.removeStubMapping(state.getProxyMapping());
    }

    if (serveEvents.isEmpty()) {
      return SnapshotRecordResult.empty();
    }

    int startIndex =
        state.getStartingServeEventId() == null
            ? serveEvents.size()
            : indexOf(serveEvents, withId(state.getStartingServeEventId()));
    int endIndex = indexOf(serveEvents, withId(state.getFinishingServeEventId()));
    List<ServeEvent> eventsToSnapshot = serveEvents.subList(endIndex, startIndex);

    SnapshotRecordResult result = takeSnapshot(eventsToSnapshot, state.getSpec());

    notifier().info("Stopped recording. Stubs captured:\n" + Json.write(result.getStubMappings()));
    return result;
  }

  private static Predicate<ServeEvent> withId(final UUID id) {
    return input -> input.getId().equals(id);
  }

  public SnapshotRecordResult takeSnapshot(List<ServeEvent> serveEvents, RecordSpec recordSpec) {
    final List<StubMapping> stubMappings =
        serveEventsToStubMappings(
            serveEvents,
            recordSpec.getFilters(),
            new SnapshotStubMappingGenerator(
                recordSpec.getCaptureHeaders(), recordSpec.getRequestBodyPatternFactory()),
            getStubMappingPostProcessor(recordSpec));

    for (StubMapping stubMapping : stubMappings) {
      if (recordSpec.shouldPersist()) {
        stubMapping.setPersistent(true);
      }
      admin.addStubMapping(stubMapping);
    }

    return recordSpec.getOutputFormat().format(stubMappings);
  }

  public List<StubMapping> serveEventsToStubMappings(
      List<ServeEvent> serveEventsResult,
      ProxiedServeEventFilters serveEventFilters,
      SnapshotStubMappingGenerator stubMappingGenerator,
      SnapshotStubMappingPostProcessor stubMappingPostProcessor) {
    final List<StubMapping> stubMappings =
        serveEventsResult.stream()
            .filter(serveEventFilters)
            .map(stubMappingGenerator)
            .collect(Collectors.toList());

    return stubMappingPostProcessor.process(stubMappings);
  }

  public SnapshotStubMappingPostProcessor getStubMappingPostProcessor(RecordSpec recordSpec) {
    final SnapshotStubMappingTransformerRunner transformerRunner =
        new SnapshotStubMappingTransformerRunner(
            extensions.ofType(StubMappingTransformer.class).values(),
            recordSpec.getTransformers(),
            recordSpec.getTransformerParameters(),
            filesBlobStore);

    return new SnapshotStubMappingPostProcessor(
        recordSpec.shouldRecordRepeatsAsScenarios(),
        transformerRunner,
        recordSpec.getExtractBodyCriteria(),
        new SnapshotStubMappingBodyExtractor(filesBlobStore));
  }

  public RecordingStatus getStatus() {
    return stateStore.get().getStatus();
  }
}
