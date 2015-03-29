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
