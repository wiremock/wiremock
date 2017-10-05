package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import java.util.List;
import java.util.Map;

public class GetScenariosResult {

    private final List<Scenario> scenarios;

    @JsonCreator
    public GetScenariosResult(@JsonProperty("scenarios") List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }
}
