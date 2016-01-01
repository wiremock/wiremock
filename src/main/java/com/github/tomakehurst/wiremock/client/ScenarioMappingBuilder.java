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
