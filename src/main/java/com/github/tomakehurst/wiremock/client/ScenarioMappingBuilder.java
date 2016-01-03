package com.github.tomakehurst.wiremock.client;

public interface ScenarioMappingBuilder<S extends ScenarioMappingBuilder> extends RemoteMappingBuilder<S, S> {

    S atPriority(Integer priority);
    S withHeader(String key, ValueMatchingStrategy headerMatchingStrategy);
    S withQueryParam(String key, ValueMatchingStrategy queryParamMatchingStrategy);
    S withRequestBody(ValueMatchingStrategy bodyMatchingStrategy);
    S inScenario(String scenarioName);

    S willReturn(ResponseDefinitionBuilder responseDefBuilder);

    S whenScenarioStateIs(String stateName);
    S willSetStateTo(String stateName);
}
