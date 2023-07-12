/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.Pair.pair;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.StubMappingStore;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractStubMappings implements StubMappings {

  protected final Scenarios scenarios;
  protected final Map<String, RequestMatcherExtension> customMatchers;
  protected final Map<String, ResponseDefinitionTransformer> transformers;
  protected final Map<String, ResponseDefinitionTransformerV2> v2transformers;
  protected final FileSource filesFileSource;
  protected final List<StubLifecycleListener> stubLifecycleListeners;
  protected final StubMappingStore store;
  protected final boolean failIfMultipleMappingsMatch;

  public AbstractStubMappings(
      final StubMappingStore store,
      final Scenarios scenarios,
      final Map<String, RequestMatcherExtension> customMatchers,
      final Map<String, ResponseDefinitionTransformer> transformers,
      final Map<String, ResponseDefinitionTransformerV2> v2transformers,
      final BlobStore filesBlobStore,
      final List<StubLifecycleListener> stubLifecycleListeners,
      final boolean failIfMultipleMappingsMatch) {

    this.store = store;
    this.scenarios = scenarios;
    this.customMatchers = customMatchers;
    this.transformers = transformers;
    this.v2transformers = v2transformers;
    this.filesFileSource = new BlobStoreFileSource(filesBlobStore);
    this.stubLifecycleListeners = stubLifecycleListeners;
    this.failIfMultipleMappingsMatch = failIfMultipleMappingsMatch;
  }

  @Override
  public ServeEvent serveFor(final ServeEvent initialServeEvent) {
    final LoggedRequest request = initialServeEvent.getRequest();

    final List<SubEvent> subEvents = new LinkedList<>();

    final StubMapping matchingMapping = this.getMatchingMapping(request, subEvents);

    subEvents.forEach(initialServeEvent::appendSubEvent);

    this.scenarios.onStubServed(matchingMapping);

    ResponseDefinition responseDefinition =
        this.applyV1Transformations(
            request,
            matchingMapping.getResponse(),
            ImmutableList.copyOf(this.transformers.values()));

    ServeEvent serveEvent =
        initialServeEvent
            .withStubMapping(matchingMapping)
            .withResponseDefinition(responseDefinition);

    final Pair<ServeEvent, ResponseDefinition> transformed =
        this.applyV2Transformations(serveEvent, ImmutableList.copyOf(this.v2transformers.values()));
    serveEvent = transformed.a;
    responseDefinition = transformed.b;

    return serveEvent.withResponseDefinition(copyOf(responseDefinition));
  }

  private StubMapping getMatchingMapping(
      final LoggedRequest request, final List<SubEvent> subEvents) {
    final List<StubMapping> stubMappings =
        this.store
            .findAllMatchingRequest(request, this.customMatchers, subEvents::add)
            .filter(
                stubMapping ->
                    stubMapping.isIndependentOfScenarioState()
                        || this.scenarios.mappingMatchesScenarioState(stubMapping))
            .collect(Collectors.toList());

    if (stubMappings.size() == 1 || !this.failIfMultipleMappingsMatch && stubMappings.size() > 1) {
      return stubMappings.get(0);
    } else if (this.failIfMultipleMappingsMatch && stubMappings.size() > 1) {
      final String found =
          stubMappings.stream()
              .map(StubMapping::getUuid)
              .map(UUID::toString)
              .sorted()
              .collect(Collectors.joining(", "));
      throw new IllegalStateException("Several mappings matched the request: " + found);
    }
    return StubMapping.NOT_CONFIGURED;
  }

  private ResponseDefinition applyV1Transformations(
      final Request request,
      final ResponseDefinition responseDefinition,
      final List<ResponseDefinitionTransformer> transformers) {

    if (transformers.isEmpty()) {
      return responseDefinition;
    }

    final ResponseDefinitionTransformer transformer = transformers.get(0);
    final ResponseDefinition newResponseDef =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(
                request,
                responseDefinition,
                this.filesFileSource,
                getFirstNonNull(responseDefinition.getTransformerParameters(), Parameters.empty()))
            : responseDefinition;

    return this.applyV1Transformations(
        request, newResponseDef, transformers.subList(1, transformers.size()));
  }

  private Pair<ServeEvent, ResponseDefinition> applyV2Transformations(
      final ServeEvent serveEvent, final List<ResponseDefinitionTransformerV2> transformers) {

    final ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

    if (transformers.isEmpty()) {
      return pair(serveEvent, responseDefinition);
    }

    final ResponseDefinitionTransformerV2 transformer = transformers.get(0);
    final ResponseDefinition newResponseDef =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(serveEvent)
            : responseDefinition;

    return this.applyV2Transformations(
        serveEvent.withResponseDefinition(newResponseDef),
        transformers.subList(1, transformers.size()));
  }

  @Override
  public void addMapping(final StubMapping mapping) {
    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.beforeStubCreated(mapping);
    }

    this.store.add(mapping);
    this.scenarios.onStubMappingAdded(mapping);

    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.afterStubCreated(mapping);
    }
  }

  @Override
  public void removeMapping(final StubMapping mapping) {
    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.beforeStubRemoved(mapping);
    }

    this.store.remove(mapping);
    this.scenarios.onStubMappingRemoved(mapping);

    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.afterStubRemoved(mapping);
    }
  }

  @Override
  public void editMapping(final StubMapping stubMapping) {
    final Optional<StubMapping> optionalExistingMapping = this.store.get(stubMapping.getId());

    if (optionalExistingMapping.isEmpty()) {
      final String msg = "StubMapping with UUID: " + stubMapping.getUuid() + " not found";
      notifier().error(msg);
      throw new NotFoundException(msg);
    }

    final StubMapping existingMapping = optionalExistingMapping.get();
    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.beforeStubEdited(existingMapping, stubMapping);
    }

    stubMapping.setInsertionIndex(existingMapping.getInsertionIndex());
    stubMapping.setDirty(true);

    this.store.replace(existingMapping, stubMapping);
    this.scenarios.onStubMappingUpdated(existingMapping, stubMapping);

    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.afterStubEdited(existingMapping, stubMapping);
    }
  }

  @Override
  public void reset() {
    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.beforeStubsReset();
    }

    this.store.clear();
    this.scenarios.clear();

    for (final StubLifecycleListener listener : this.stubLifecycleListeners) {
      listener.afterStubsReset();
    }
  }

  @Override
  public void resetScenarios() {
    this.scenarios.reset();
  }

  @Override
  public List<StubMapping> getAll() {
    return ImmutableList.copyOf(this.store.getAll().collect(toList()));
  }

  @Override
  public Optional<StubMapping> get(final UUID id) {
    return this.store.get(id);
  }

  @Override
  public List<Scenario> getAllScenarios() {
    return this.scenarios.getAll();
  }

  @Override
  public List<StubMapping> findByMetadata(final StringValuePattern pattern) {
    return this.store
        .getAll()
        .filter(
            stubMapping -> {
              final String metadataJson = Json.write(stubMapping.getMetadata());
              return pattern.match(metadataJson).isExactMatch();
            })
        .collect(toList());
  }
}
