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
import com.github.tomakehurst.wiremock.matching.RequestMatcher;

public class ScenarioMappingBuilder extends MappingBuilder<ScenarioMappingBuilder> {

    public ScenarioMappingBuilder(RequestMethod method, UrlMatchingStrategy urlMatchingStrategy) {
        super(method, urlMatchingStrategy);
    }

    public ScenarioMappingBuilder(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    public ScenarioMappingBuilder(String customRequestMatcherName, Parameters parameters) {
        super(customRequestMatcherName, parameters);
    }

    public ScenarioMappingBuilder whenScenarioStateIs(String stateName) {
        this.requiredScenarioState = stateName;
        return this;
    }

    public ScenarioMappingBuilder willSetStateTo(String stateName) {
        this.newScenarioState = stateName;
        return this;
    }
}
