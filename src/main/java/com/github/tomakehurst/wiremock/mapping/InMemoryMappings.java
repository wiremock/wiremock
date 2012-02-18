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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.mapping.RequestResponseMapping.NOT_CONFIGURED;
import static com.github.tomakehurst.wiremock.mapping.ResponseDefinition.copyOf;
import static com.google.common.collect.Iterables.find;

import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;


public class InMemoryMappings implements Mappings {
	
	private final SortedConcurrentMappingSet mappings = new SortedConcurrentMappingSet();
	private final ConcurrentHashMap<String, Scenario> scenarioMap = new ConcurrentHashMap<String, Scenario>();
	
	@Override
	public ResponseDefinition serveFor(Request request) {
		RequestResponseMapping matchingMapping = find(
				mappings,
				mappingMatchingAndInCorrectScenarioState(request),
				RequestResponseMapping.NOT_CONFIGURED);
		
		notifyIfResponseNotConfigured(request, matchingMapping);
		matchingMapping.updateScenarioStateIfRequired();
		return copyOf(matchingMapping.getResponse());
	}

	private void notifyIfResponseNotConfigured(Request request, RequestResponseMapping matchingMapping) {
		if (matchingMapping == NOT_CONFIGURED) {
		    notifier().info("No mapping found matching URL " + request.getUrl());
		}
	}

	@Override
	public void addMapping(RequestResponseMapping mapping) {
		if (mapping.isInScenario()) {
			scenarioMap.putIfAbsent(mapping.getScenarioName(), Scenario.inStartedState());
			Scenario scenario = scenarioMap.get(mapping.getScenarioName());
			mapping.setScenario(scenario);
		}
		
		mappings.add(mapping);
	}

	@Override
	public void reset() {
		mappings.clear();
	}
	
	@Override
	public void resetScenarios() {
		for (Scenario scenario: scenarioMap.values()) {
			scenario.reset();
		}
	}
	
	private Predicate<RequestResponseMapping> mappingMatchingAndInCorrectScenarioState(final Request request) {
		return new Predicate<RequestResponseMapping>() {
			public boolean apply(RequestResponseMapping mapping) {
				return mapping.getRequest().isMatchedBy(request) &&
				(mapping.isIndependentOfScenarioState() || mapping.requiresCurrentScenarioState());
			}
		};
	}
}
