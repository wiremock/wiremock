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

import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class LocalMappingBuilder {

    private MappingBuilder mappingBuilder;

    public LocalMappingBuilder(RequestMatcher requestMatcher) {
        mappingBuilder = new MappingBuilder(requestMatcher);
    }

    public LocalMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
        mappingBuilder.willReturn(responseDefBuilder);
        return this;
    }

    public LocalMappingBuilder withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
        mappingBuilder.withHeader(key, headerMatchingStrategy);
        return this;
    }

    public LocalMappingBuilder withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
        mappingBuilder.withRequestBody(bodyMatchingStrategy);
        return this;
    }

    public LocalMappingBuilder whenScenarioStateIs(String stateName) {
        mappingBuilder.whenScenarioStateIs(stateName);
        return this;
    }

    public LocalMappingBuilder inScenario(String scenarioName) {
        mappingBuilder.inScenario(scenarioName);
        return this;
    }

    public LocalMappingBuilder withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        mappingBuilder.withQueryParam(key, queryParamMatchingStrategy);
        return this;
    }

    public LocalMappingBuilder atPriority(Integer priority) {
        mappingBuilder.atPriority(priority);
        return this;
    }

    public LocalMappingBuilder willSetStateTo(String stateName) {
        mappingBuilder.willSetStateTo(stateName);
        return this;
    }

    public StubMapping build() {
        return mappingBuilder.build();
    }
}
