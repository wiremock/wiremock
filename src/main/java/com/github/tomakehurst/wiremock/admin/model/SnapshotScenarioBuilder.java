package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates scenarios for repeated requests.
 */
public class SnapshotScenarioBuilder {
    private final Map<RequestPattern, ScenarioDetails> tracker = new HashMap<>();

    public void addToScenario(StubMapping stubMapping) {
        ScenarioDetails details = tracker.get(stubMapping.getRequest());

        if (details == null) {
            details = new ScenarioDetails(stubMapping.getRequest());
            tracker.put(stubMapping.getRequest(), details);
        }

        details.setScenarioDetails(stubMapping);
    }

    /**
     * Simple container class for building scenarios. Tracks the number of times a request has been seen, the associated
     * scenario name, and the current scenario state;
     */
    private static class ScenarioDetails {
        private final static String SCENARIO_NAME_PREFIX = "scenario";
        private final String name;
        private int count = 1;
        private String currentState = Scenario.STARTED;

        private ScenarioDetails(RequestPattern request) {
            this.name = SCENARIO_NAME_PREFIX + "-" + Urls.urlToPathParts(URI.create(request.getUrl()));
        }

        private void setScenarioDetails(StubMapping stubMapping) {
            stubMapping.setScenarioName(name);
            stubMapping.setRequiredScenarioState(currentState);

            if (count > 1) {
                currentState = name + "-" + count;
                stubMapping.setNewScenarioState(currentState);
            }
            count += 1;
        }
    }
}
