/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;


public class NewInMemoryStubMappings implements StubMappings {

    public static final int MAX_NEAR_MISSES = 3;

    private final SortedConcurrentMappingSet mappings = new SortedConcurrentMappingSet();
    private final ConcurrentHashMap<String, Scenario> scenarioMap = new ConcurrentHashMap<String, Scenario>();
    private final Map<String, RequestMatcherExtension> customMatchers;
    private final RequestJournal requestJournal;
    private final Map<String, ResponseDefinitionTransformer> transformers;
    private final FileSource rootFileSource;

    public NewInMemoryStubMappings(Map<String, RequestMatcherExtension> customMatchers, RequestJournal requestJournal, Map<String, ResponseDefinitionTransformer> transformers, FileSource rootFileSource) {
        this.customMatchers = customMatchers;
        this.requestJournal = requestJournal;
        this.transformers = transformers;
        this.rootFileSource = rootFileSource;
    }

    @Override
    public ServedStub serveFor(Request request) {
        List<NearMiss> nearMisses = newArrayList();
        LoggedRequest loggedRequest = LoggedRequest.createFrom(request);

        for (StubMapping mapping : mappings) {
            MatchResult matchResult = mapping.getNewRequest().match(request);

            if (matchResult.isExactMatch()) {
                ServedStub servedStub = ServedStub.exactMatch(loggedRequest, mapping.getResponse());
                requestJournal.requestReceived(servedStub);
                return servedStub;
            } else {
                nearMisses.add(new NearMiss(loggedRequest, mapping, matchResult));
            }
        }

        Collections.sort(nearMisses);

        ServedStub servedStub = ServedStub.noExactMatch(loggedRequest);
        requestJournal.requestReceived(servedStub);
        return servedStub;
    }

    private ResponseDefinition applyTransformations(Request request,
                                                    ResponseDefinition responseDefinition,
                                                    List<ResponseDefinitionTransformer> transformers) {
        if (transformers.isEmpty()) {
            return responseDefinition;
        }

        ResponseDefinitionTransformer transformer = transformers.get(0);
        ResponseDefinition newResponseDef =
            transformer.applyGlobally() || responseDefinition.hasTransformer(transformer) ?
                transformer.transform(request, responseDefinition, rootFileSource.child(FILES_ROOT), responseDefinition.getTransformerParameters()) :
                responseDefinition;

        return applyTransformations(request, newResponseDef, transformers.subList(1, transformers.size()));
    }

    @Override
    public void addMapping(StubMapping mapping) {

        updateSenarioMapIfPresent(mapping);
        mappings.add(mapping);
    }

    @Override
    public void editMapping(StubMapping stubMapping) {

        final Optional<StubMapping> optionalExistingMapping = tryFind(
            mappings,
            mappingMatchingUuid(stubMapping.getUuid())
        );

        if (!optionalExistingMapping.isPresent()) {
            String msg = "StubMapping with UUID: " + stubMapping.getUuid() + " not found";
            notifier().error(msg);
            throw new RuntimeException(msg);
        }

        final StubMapping existingMapping = optionalExistingMapping.get();

        updateSenarioMapIfPresent(stubMapping);
        stubMapping.setInsertionIndex(existingMapping.getInsertionIndex());
        stubMapping.setTransient(true);

        mappings.replace(existingMapping, stubMapping);
    }

    private void updateSenarioMapIfPresent(StubMapping mapping) {
        if (mapping.isInScenario()) {
            scenarioMap.putIfAbsent(mapping.getScenarioName(), Scenario.inStartedState());
            Scenario scenario = scenarioMap.get(mapping.getScenarioName());
            mapping.setScenario(scenario);
        }
    }

    @Override
    public void reset() {
        mappings.clear();
        scenarioMap.clear();
    }

    @Override
    public void resetScenarios() {
        for (Scenario scenario : scenarioMap.values()) {
            scenario.reset();
        }
    }

    @Override
    public List<StubMapping> getAll() {
        return ImmutableList.copyOf(mappings);
    }

    private Predicate<StubMapping> mappingMatchingAndInCorrectScenarioState(final Request request) {
        return new Predicate<StubMapping>() {
            public boolean apply(StubMapping mapping) {
                return mapping.getNewRequest().isMatchedBy(request, customMatchers) &&
                    (mapping.isIndependentOfScenarioState() || mapping.requiresCurrentScenarioState());
            }
        };
    }

    private Predicate<StubMapping> mappingMatchingUuid(final UUID uuid) {
        return new Predicate<StubMapping>() {
            @Override
            public boolean apply(StubMapping input) {
                return input.getUuid().equals(uuid);
            }
        };
    }
}
