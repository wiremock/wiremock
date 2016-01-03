package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface RemoteMappingBuilder<M extends RemoteMappingBuilder, S extends ScenarioMappingBuilder> {
    M atPriority(Integer priority);
    M withHeader(String key, ValueMatchingStrategy headerMatchingStrategy);
    M withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy);
    M withRequestBody(ValueMatchingStrategy bodyMatchingStrategy);
    S inScenario(String scenarioName);

    M willReturn(ResponseDefinitionBuilder responseDefBuilder);

    StubMapping build();
}
