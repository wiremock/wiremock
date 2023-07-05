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
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.store.BlobStore;
import com.github.tomakehurst.wiremock.store.StubMappingStore;
import com.github.tomakehurst.wiremock.store.files.BlobStoreFileSource;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractStubMappings implements StubMappings {

  protected final Scenarios scenarios;
  protected final Map<String, RequestMatcherExtension> customMatchers;
  protected final Map<String, ResponseDefinitionTransformer> transformers;
  protected final FileSource filesFileSource;
  protected final List<StubLifecycleListener> stubLifecycleListeners;
  protected final StubMappingStore store;

  public AbstractStubMappings(
      StubMappingStore store,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers,
      Map<String, ResponseDefinitionTransformer> transformers,
      BlobStore filesBlobStore,
      List<StubLifecycleListener> stubLifecycleListeners) {

    this.store = store;
    this.scenarios = scenarios;
    this.customMatchers = customMatchers;
    this.transformers = transformers;
    this.filesFileSource = new BlobStoreFileSource(filesBlobStore);
    this.stubLifecycleListeners = stubLifecycleListeners;
  }

  @Override
  public ServeEvent serveFor(Request request) {
    StubMapping matchingMapping =
        store
            .findAllMatchingRequest(request, customMatchers)
            .filter(
                stubMapping ->
                    stubMapping.isIndependentOfScenarioState()
                        || scenarios.mappingMatchesScenarioState(stubMapping))
            .findFirst()
            .orElse(StubMapping.NOT_CONFIGURED);

    scenarios.onStubServed(matchingMapping);

    ServeEvent serveEvent = ServeEvent.of(request, matchingMapping);
    ServeEvent.setCurrent(serveEvent);

    ResponseDefinition responseDefinition =
        applyTransformations(
            request, matchingMapping.getResponse(), ImmutableList.copyOf(transformers.values()));

    serveEvent = serveEvent.withResponseDefinition(copyOf(responseDefinition));
    ServeEvent.setCurrent(serveEvent);

    return serveEvent;
  }

  private ResponseDefinition applyTransformations(
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

    return applyTransformations(
        request, newResponseDef, transformers.subList(1, transformers.size()));
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
    return ImmutableList.copyOf(store.getAll().collect(toList()));
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
