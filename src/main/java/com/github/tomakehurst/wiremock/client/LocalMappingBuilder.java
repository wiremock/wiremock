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

public abstract class LocalMappingBuilder<T extends LocalMappingBuilder<?>> {

    protected ScenarioMappingBuilder mappingBuilder;

    public LocalMappingBuilder(RequestMatcher requestMatcher) {
        mappingBuilder = new ScenarioMappingBuilder(requestMatcher);
    }

    @SuppressWarnings("unchecked")
    public T willReturn(ResponseDefinitionBuilder responseDefBuilder) {
        mappingBuilder.willReturn(responseDefBuilder);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withHeader(String key, ValueMatchingStrategy headerMatchingStrategy) {
        mappingBuilder.withHeader(key, headerMatchingStrategy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withRequestBody(ValueMatchingStrategy bodyMatchingStrategy) {
        mappingBuilder.withRequestBody(bodyMatchingStrategy);
        return (T) this;
    }

    public LocalScenarioMappingBuilder inScenario(String scenarioName) {
        mappingBuilder.inScenario(scenarioName);
        return (LocalScenarioMappingBuilder)this;
    }

    @SuppressWarnings("unchecked")
    public T withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy) {
        mappingBuilder.withQueryParam(key, queryParamMatchingStrategy);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T atPriority(Integer priority) {
        mappingBuilder.atPriority(priority);
        return (T) this;
    }

    public StubMapping build() {
        return mappingBuilder.build();
    }
}
