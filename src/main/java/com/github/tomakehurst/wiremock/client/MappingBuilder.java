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

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class MappingBuilder<T extends MappingBuilder<?>> {
	
	private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Integer priority;
	private String scenarioName;
	protected String requiredScenarioState;
	protected String newScenarioState;
	
	public MappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
		requestPatternBuilder = new RequestPatternBuilder(method, urlMatchingStrategy);
	}

	public MappingBuilder(RequestMatcher requestMatcher) {
		requestPatternBuilder = RequestPatternBuilder.forCustomMatcher(requestMatcher);
	}

	public MappingBuilder(String customRequestMatcherName, Parameters parameters) {
		requestPatternBuilder = RequestPatternBuilder.forCustomMatcher(customRequestMatcherName, parameters);
	}

	@SuppressWarnings("unchecked")
	public T willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T atPriority(Integer priority) {
		this.priority = priority;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
		requestPatternBuilder.withHeader(key, headerMatchingStrategy);
		return (T) this;
	}

    @SuppressWarnings("unchecked")
    public T withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        requestPatternBuilder.withQueryParam(key, queryParamMatchingStrategy);
        return (T) this;
    }

	@SuppressWarnings("unchecked")
	public T withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
		requestPatternBuilder.withRequestBody(bodyMatchingStrategy);
		return (T) this;
	}

	public ScenarioMappingBuilder inScenario(String scenarioName) {
        checkArgument(scenarioName != null, "Scenario name must not be null");

		this.scenarioName = scenarioName;
		return (ScenarioMappingBuilder) this;
	}

	public StubMapping build() {
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
