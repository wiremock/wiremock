package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.UniqueFilenameGenerator;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Counts unique RequestPatterns from StubMappings. If shouldRecordRepeatsAsScenarios is enabled, then multiple
 * identical requests will be recorded as scenarios. Otherwise, they're skipped.
 */
public class SnapshotRepeatedRequestHandler {
    private final static String SCENARIO_NAME_PREFIX = "scenario";
    private final boolean shouldRecordRepeatsAsScenarios;
    private final HashMap<RequestPattern, StubMappingTracker> requestStubMappingTracker;

    public SnapshotRepeatedRequestHandler(boolean shouldRecordRepeatsAsScenarios) {
        this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
        this.requestStubMappingTracker = new HashMap<>();
    }

    public List<StubMapping> processStubMappings(Iterable<StubMapping> stubMappings) {
        this.requestStubMappingTracker.clear();
        ArrayList<StubMapping> processedMappings = new ArrayList<>();

        for (StubMapping stubMapping : stubMappings) {
            StubMappingTracker tracker = requestStubMappingTracker.get(stubMapping.getRequest());

            // If tracker is null, this request has not been seen before. Otherwise, it's a repeat.
            if (tracker == null || shouldRecordRepeatsAsScenarios) {
                if (tracker == null) {
                    requestStubMappingTracker.put(stubMapping.getRequest(), new StubMappingTracker(stubMapping));
                } else {
                    tracker.count++;
                    setScenarioDetailsIfApplicable(stubMapping, tracker);
                    tracker.previousStubMapping = stubMapping;
                }
                processedMappings.add(stubMapping);
            }
        }

        return processedMappings;
    }

    private void setScenarioDetailsIfApplicable(StubMapping stubMapping, StubMappingTracker tracker) {
        if (tracker.count == 2) {
            // We have multiple identical requests. Go back and make previous stub the start
            String name = SCENARIO_NAME_PREFIX + "-" + UniqueFilenameGenerator.urlToPathParts(
                stubMapping.getRequest().getUrl()
            );
            tracker.previousStubMapping.setScenarioName(name);
            tracker.previousStubMapping.setRequiredScenarioState(Scenario.STARTED);
            stubMapping.setRequiredScenarioState(Scenario.STARTED);
        } else {
            String previousState = tracker.previousStubMapping.getNewScenarioState();
            stubMapping.setRequiredScenarioState(previousState);
        }

        String name = tracker.previousStubMapping.getScenarioName();
        stubMapping.setScenarioName(name);
        stubMapping.setNewScenarioState(name + "-" + tracker.count);
    }

    /**
     * Simple container class to store the previous stub mapping and sequence count for building scenarios
     */
    private class StubMappingTracker {
        private int count;
        private StubMapping previousStubMapping;

        public StubMappingTracker(StubMapping stubMapping) {
            this.count = 1;
            this.previousStubMapping = stubMapping;
        }
    }
}
