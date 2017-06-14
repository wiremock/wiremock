package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.net.URI;
import java.util.HashMap;

import static java.lang.Math.min;

/**
 * Tracks RequestPatterns from StubMappings and generates scenarios when multiple identical requests are seen.
 */
public class SnapshotStubMappingScenarioHandler {
    private final static String SCENARIO_NAME_PREFIX = "scenario";

    private final HashMap<RequestPattern, StubMappingTracker> requestStubMappingTracker;

    public SnapshotStubMappingScenarioHandler() {
        requestStubMappingTracker = new HashMap<>();
    }

    public void reset() {
        this.requestStubMappingTracker.clear();
    }

    public void trackStubMapping(StubMapping stubMapping) {
        StubMappingTracker tracker = requestStubMappingTracker.get(stubMapping.getRequest());

        if (tracker == null) {
            requestStubMappingTracker.put(stubMapping.getRequest(), new StubMappingTracker(stubMapping));
            return;
        }

        tracker.count++;

        if (tracker.count == 2) {
            // We have multiple identical requests. Go back and make previous stub the start
            String name = generateScenarioName(stubMapping.getRequest());
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

        tracker.previousStubMapping = stubMapping;
    }

    /**
     * Generates a scenario name from the request. Based on UniqueFilenameGenerator
     *
     * @TODO Use a better name generator
     * @param request A RequestPattern from a StubMapping
     * @return Scenario name as a string
     */
    private String generateScenarioName(RequestPattern request) {
        final URI uri = URI.create(request.getUrl());
        final Iterable<String> uriPathNodes = Splitter
            .on("/")
            .omitEmptyStrings()
            .split(uri.getPath());

        final int nodeCount = Iterables.size(uriPathNodes);

        String pathPart = "(root)";
        if (nodeCount > 0) {
            pathPart = Joiner
                .on("-")
                .join(
                    Iterables.skip(uriPathNodes, nodeCount - min(nodeCount, 2))
                );
            pathPart = sanitise(pathPart);
        }

        return SCENARIO_NAME_PREFIX + "-" + pathPart;
    }

    private static String sanitise(String input) {
        return input.replaceAll("[,~:/?#\\[\\]@!\\$&'()*+;=]", "_");
    }

    /**
     * Simple container class to store the previous stub mapping and sequence count for the scenario
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
