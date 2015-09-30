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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.copyOf;
import static com.github.tomakehurst.wiremock.stubbing.StubMapping.NOT_CONFIGURED;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.tryFind;


public class InMemoryStubMappings implements StubMappings {
	
	private final SortedConcurrentMappingSet mappings = new SortedConcurrentMappingSet();
	private final ConcurrentHashMap<String, Scenario> scenarioMap = new ConcurrentHashMap<String, Scenario>();
	
	@Override
	public ResponseDefinition serveFor(Request request) {
		StubMapping matchingMapping = find(
				mappings,
				mappingMatchingAndInCorrectScenarioState(request),
				StubMapping.NOT_CONFIGURED);
		
		notifyIfResponseNotConfigured(request, matchingMapping);
		matchingMapping.updateScenarioStateIfRequired();
		return copyOf(matchingMapping.getResponse());
	}

	private void notifyIfResponseNotConfigured(Request request, StubMapping matchingMapping) {
		if (matchingMapping == NOT_CONFIGURED) {
		    notifier().info("No mapping found matching URL " + request.getUrl());
		}
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
		stubMapping.setMappingFileName(existingMapping.getMappingFileName());
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
		for (Scenario scenario: scenarioMap.values()) {
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
				return mapping.getRequest().isMatchedBy(request) &&
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
