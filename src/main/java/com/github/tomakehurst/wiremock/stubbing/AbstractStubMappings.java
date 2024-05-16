/*
 * Copyright (C) 2022-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.extension.ServeEventListener.RequestPhase.AFTER_MATCH;
import static com.github.tomakehurst.wiremock.extension.ServeEventListenerUtils.triggerListeners;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.StubMappingStore;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.*;

public abstract class AbstractStubMappings implements StubMappings {

  protected final StubMappingStore store;
  protected final Scenarios scenarios;
  protected final Map<String, RequestMatcherExtension> customMatchers;
  protected final Map<String, ResponseDefinitionTransformer> transformers;
  protected final Map<String, ResponseDefinitionTransformerV2> v2transformers;
  protected final FileSource filesFileSource;
  protected final List<StubLifecycleListener> stubLifecycleListeners;
  protected final Map<String, ServeEventListener> serveEventListeners;

  public AbstractStubMappings(
      StubMappingStore store,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers,
      Map<String, ResponseDefinitionTransformer> transformers,
      Map<String, ResponseDefinitionTransformerV2> v2transformers,
      BlobStore filesBlobStore,
      List<StubLifecycleListener> stubLifecycleListeners,
      Map<String, ServeEventListener> serveEventListeners) {
    this.store = store;
    this.scenarios = scenarios;
    this.customMatchers = customMatchers;
    this.transformers = transformers;
    this.v2transformers = v2transformers;
    this.filesFileSource = new BlobStoreFileSource(filesBlobStore);
    this.stubLifecycleListeners = stubLifecycleListeners;
    this.serveEventListeners = serveEventListeners;
  }

  @Override
  public ServeEvent serveFor(final ServeEvent initialServeEvent) {
    final LoggedRequest request = initialServeEvent.getRequest();

    final List<SubEvent> subEvents = new LinkedList<>();

    StubMapping matchingStub =
        store
            .findAllMatchingRequest(request, customMatchers, subEvents::add)
            .filter(
                stubMapping ->
                    stubMapping.isIndependentOfScenarioState()
                        || scenarios.mappingMatchesScenarioState(stubMapping))
            .findFirst()
            .orElse(StubMapping.NOT_CONFIGURED);

    subEvents.forEach(initialServeEvent::appendSubEvent);

    scenarios.onStubServed(matchingStub);

    final ResponseDefinition initialResponseDefinition = matchingStub.getResponse();
    ServeEvent serveEvent =
        initialServeEvent
            .withStubMapping(matchingStub)
            .withResponseDefinition(initialResponseDefinition);

    triggerListeners(serveEventListeners, AFTER_MATCH, serveEvent);

    ResponseDefinition responseDefinition =
        applyV1Transformations(
            request, matchingStub.getResponse(), List.copyOf(transformers.values()));

    serveEvent = serveEvent.withResponseDefinition(responseDefinition);

    final Pair<ServeEvent, ResponseDefinition> transformed =
        applyV2Transformations(serveEvent, List.copyOf(v2transformers.values()));
    serveEvent = transformed.a;
    responseDefinition = transformed.b;

    return serveEvent.withResponseDefinition(copyOf(responseDefinition));
  }

  private ResponseDefinition applyV1Transformations(
      Request request,
      ResponseDefinition responseDefinition,
      List<ResponseDefinitionTransformer> transformers) {

    if (transformers.isEmpty()) {
      return responseDefinition;
    }

    ResponseDefinitionTransformer transformer = transformers.get(0);
    ResponseDefinition newResponseDef =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(
                request,
                responseDefinition,
                filesFileSource,
                getFirstNonNull(responseDefinition.getTransformerParameters(), Parameters.empty()))
            : responseDefinition;

    return applyV1Transformations(
        request, newResponseDef, transformers.subList(1, transformers.size()));
  }

  private Pair<ServeEvent, ResponseDefinition> applyV2Transformations(
      ServeEvent serveEvent, List<ResponseDefinitionTransformerV2> transformers) {

    final ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

    if (transformers.isEmpty()) {
      return pair(serveEvent, responseDefinition);
    }

    ResponseDefinitionTransformerV2 transformer = transformers.get(0);
    ResponseDefinition newResponseDef =
        transformer.applyGlobally() || responseDefinition.hasTransformer(transformer)
            ? transformer.transform(serveEvent)
            : responseDefinition;

    return applyV2Transformations(
        serveEvent.withResponseDefinition(newResponseDef),
        transformers.subList(1, transformers.size()));
  }

  @Override
  public void addMapping(StubMapping mapping) {
    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.beforeStubCreated(mapping);
    }

    store.add(mapping);
    scenarios.onStubMappingAdded(mapping);

    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.afterStubCreated(mapping);
    }
  }

  @Override
  public void removeMapping(StubMapping mapping) {
    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.beforeStubRemoved(mapping);
    }

    store.remove(mapping);
    scenarios.onStubMappingRemoved(mapping);

    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.afterStubRemoved(mapping);
    }
  }

  @Override
  public void editMapping(StubMapping stubMapping) {
    final Optional<StubMapping> optionalExistingMapping = store.get(stubMapping.getId());

    if (optionalExistingMapping.isEmpty()) {
      String msg = "StubMapping with UUID: " + stubMapping.getUuid() + " not found";
      notifier().error(msg);
      throw new NotFoundException(msg);
    }

    final StubMapping existingMapping = optionalExistingMapping.get();
    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.beforeStubEdited(existingMapping, stubMapping);
    }

    stubMapping.setInsertionIndex(existingMapping.getInsertionIndex());
    stubMapping.setDirty(true);

    store.replace(existingMapping, stubMapping);
    scenarios.onStubMappingUpdated(existingMapping, stubMapping);

    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.afterStubEdited(existingMapping, stubMapping);
    }
  }

  @Override
  public void reset() {
    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.beforeStubsReset();
    }

    store.clear();
    scenarios.clear();

    for (StubLifecycleListener listener : stubLifecycleListeners) {
      listener.afterStubsReset();
    }
  }

  @Override
  public void resetScenarios() {
    scenarios.reset();
  }

  @Override
  public List<StubMapping> getAll() {
    return store.getAll().collect(toList());
  }

  @Override
  public Optional<StubMapping> get(final UUID id) {
    return store.get(id);
  }

  @Override
  public List<Scenario> getAllScenarios() {
    return scenarios.getAll();
  }

  @Override
  public List<StubMapping> findByMetadata(final StringValuePattern pattern) {
    return store
        .getAll()
        .filter(
            stubMapping -> {
              String metadataJson = Json.write(stubMapping.getMetadata());
              return pattern.match(metadataJson).isExactMatch();
            })
        .collect(toList());
  }
}
