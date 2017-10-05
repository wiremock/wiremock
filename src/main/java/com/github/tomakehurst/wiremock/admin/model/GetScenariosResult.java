package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import java.util.Map;

public class GetScenariosResult {

    private final Map<String, Scenario> scenarios;

    @JsonCreator
    public GetScenariosResult(@JsonProperty("scenarios") Map<String, Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public Map<String, Scenario> getScenarios() {
        return scenarios;
    }
}
