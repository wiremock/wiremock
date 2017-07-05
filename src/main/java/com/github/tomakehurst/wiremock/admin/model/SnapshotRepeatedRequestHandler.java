package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.UniqueFilenameGenerator;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.HashMap;
import java.util.Iterator;

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

    public void processStubMappingsInPlace(Iterable<StubMapping> stubMappings) {
        this.requestStubMappingTracker.clear();

        final Iterator<StubMapping> stubMappingIterator = stubMappings.iterator();
        while (stubMappingIterator.hasNext()) {
            StubMapping stubMapping = stubMappingIterator.next();
            StubMappingTracker tracker = requestStubMappingTracker.get(stubMapping.getRequest());

            // If tracker is null, this request has not been seen before. Otherwise, it's a repeat.
            if (tracker == null) {
                requestStubMappingTracker.put(stubMapping.getRequest(), new StubMappingTracker(stubMapping));
            } else if (shouldRecordRepeatsAsScenarios) {
                tracker.count++;
                setScenarioDetailsIfApplicable(stubMapping, tracker);
                tracker.previousStubMapping = stubMapping;
            } else {
                // we have a duplicate and aren't recording repeats as scenarios, so remove it
                stubMappingIterator.remove();
            }
        }
    }

    private void setScenarioDetailsIfApplicable(StubMapping stubMapping, StubMappingTracker tracker) {
        if (tracker.count == 2) {
            // Start the scenario because we have multiple identical requests. Retrieve previous stub mapping from
            // the tracker and mark it as the start.
            String name = SCENARIO_NAME_PREFIX + "-" + UniqueFilenameGenerator.urlToPathParts(
                stubMapping.getRequest().getUrl()
            );
            tracker.previousStubMapping.setScenarioName(name);
            tracker.previousStubMapping.setRequiredScenarioState(Scenario.STARTED);
            stubMapping.setRequiredScenarioState(Scenario.STARTED);
        } else {
            // More than two identical requests. Continue the scenario.
            String previousState = tracker.previousStubMapping.getNewScenarioState();
            stubMapping.setRequiredScenarioState(previousState);
        }

        String name = tracker.previousStubMapping.getScenarioName();
        stubMapping.setScenarioName(name);
        stubMapping.setNewScenarioState(name + "-" + tracker.count);
    }

    /**
     * Simple container class for building scenarios. Tracks the number of times a request has been seen and the
     * last-seen stub mapping for that request.
     */
    private static class StubMappingTracker {
        private int count;
        private StubMapping previousStubMapping;

        private StubMappingTracker(StubMapping stubMapping) {
            this.count = 1;
            this.previousStubMapping = stubMapping;
        }
    }
}
