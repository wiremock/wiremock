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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class MappingBuilder {
	
	private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Integer priority;
	private String scenarioName;
	private String requiredScenarioState;
	private String newScenarioState;
	
	public MappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		requestPatternBuilder = new RequestPatternBuilder(method, urlMatchingStrategy);
	}

	public MappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}
	
	public MappingBuilder atPriority(Integer priority) {
		this.priority = priority;
		return this;
	}
	
	public MappingBuilder withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
		requestPatternBuilder.withHeader(key, headerMatchingStrategy);
		return this;
	}

    public MappingBuilder withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        requestPatternBuilder.withQueryParam(key, queryParamMatchingStrategy);
        return this;
    }

	public MappingBuilder withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
		requestPatternBuilder.withRequestBody(bodyMatchingStrategy);
		return this;
	}

	public MappingBuilder inScenario(String scenarioName) {
		this.scenarioName = scenarioName;
		return this;
	}

	public MappingBuilder whenScenarioStateIs(String stateName) {
		this.requiredScenarioState = stateName;
		return this;
	}

	public MappingBuilder willSetStateTo(String stateName) {
		this.newScenarioState = stateName;
		return this;
	}

	public StubMapping build() {
		if (scenarioName == null && (requiredScenarioState != null || newScenarioState != null)) {
			throw new IllegalStateException("Scenario name must be specified to require or set a new scenario state");
		}
		RequestPattern requestPattern = requestPatternBuilder.build();
		ResponseDefinition response = responseDefBuilder.build();
		StubMapping mapping = new StubMapping(requestPattern, response);
		mapping.setPriority(priority);
		mapping.setScenarioName(scenarioName);
		mapping.setRequiredScenarioState(requiredScenarioState);
		mapping.setNewScenarioState(newScenarioState);
		return mapping;
	}
}
