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
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

class BasicMappingBuilder implements ScenarioMappingBuilder {

    private RequestPatternBuilder requestPatternBuilder;
	private ResponseDefinitionBuilder responseDefBuilder;
	private Integer priority;
	private String scenarioName;
	private String requiredScenarioState;
	private String newScenarioState;
	private UUID id;

	BasicMappingBuilder(RequestMethod method, UrlPattern urlPattern) {
        requestPatternBuilder = new RequestPatternBuilder(method, urlPattern);
	}

	BasicMappingBuilder(RequestMatcher requestMatcher) {
        requestPatternBuilder = new RequestPatternBuilder(requestMatcher);
	}

	BasicMappingBuilder(String customRequestMatcherName, Parameters parameters) {
		requestPatternBuilder = new RequestPatternBuilder(customRequestMatcherName, parameters);
	}

	@Override
	public BasicMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
		this.responseDefBuilder = responseDefBuilder;
		return this;
	}

	@Override
	public BasicMappingBuilder atPriority(Integer priority) {
		this.priority = priority;
		return this;
	}

	@Override
	public BasicMappingBuilder withHeader(String key, StringValuePattern headerPattern) {
        requestPatternBuilder.withHeader(key, headerPattern);
		return this;
	}

    @Override
    public BasicMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern) {
		requestPatternBuilder.withCookie(name, cookieValuePattern);
        return this;
    }

    @Override
    public BasicMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
        requestPatternBuilder.withQueryParam(key, queryParamPattern);
        return this;
    }

	@Override
	public BasicMappingBuilder withRequestBody(StringValuePattern bodyPattern) {
        requestPatternBuilder.withRequestBody(bodyPattern);
		return this;
	}

	@Override
    public BasicMappingBuilder inScenario(String scenarioName) {
        checkArgument(scenarioName != null, "Scenario name must not be null");

		this.scenarioName = scenarioName;
		return this;
	}

    @Override
	public BasicMappingBuilder whenScenarioStateIs(String stateName) {
		this.requiredScenarioState = stateName;
		return this;
	}

    @Override
	public BasicMappingBuilder willSetStateTo(String stateName) {
		this.newScenarioState = stateName;
		return this;
	}

	@Override
	public BasicMappingBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	@Override
	public BasicMappingBuilder withBasicAuth(String username, String password) {
		requestPatternBuilder.withBasicAuth(new BasicCredentials(username, password));
		return this;
	}

    @Override
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
		mapping.setUuid(id);
		return mapping;
	}

}
