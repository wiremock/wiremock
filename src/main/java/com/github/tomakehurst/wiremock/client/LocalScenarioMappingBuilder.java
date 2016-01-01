package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.matching.RequestMatcher;

public class LocalScenarioMappingBuilder extends LocalMappingBuilder<LocalScenarioMappingBuilder> {
    public LocalScenarioMappingBuilder(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    public LocalScenarioMappingBuilder whenScenarioStateIs(String stateName) {
        mappingBuilder.whenScenarioStateIs(stateName);
        return this;
    }

    public LocalScenarioMappingBuilder willSetStateTo(String stateName) {
        mappingBuilder.willSetStateTo(stateName);
        return this;
    }
}
