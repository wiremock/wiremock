package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Counts unique RequestPatterns from StubMappings. If shouldRecordRepeatsAsScenarios is enabled, then multiple
 * identical requests will be recorded as scenarios. Otherwise, they're removed.
 */
public class SnapshotRepeatedRequestHandler {
    private final static String SCENARIO_NAME_PREFIX = "scenario";
    private final boolean shouldRecordRepeatsAsScenarios;

    public SnapshotRepeatedRequestHandler(boolean shouldRecordRepeatsAsScenarios) {
        this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
    }

    public void filterOrCreateScenarios(Iterable<StubMapping> stubMappings) {
        final MultiSet<RequestPattern> requestCounts = new HashMultiSet<>();
        final Iterator<StubMapping> stubMappingIterator = stubMappings.iterator();

        while (stubMappingIterator.hasNext()) {
            StubMapping stubMapping = stubMappingIterator.next();
            requestCounts.add(stubMapping.getRequest());

            if (!shouldRecordRepeatsAsScenarios && requestCounts.getCount(stubMapping.getRequest()) > 1) {
                stubMappingIterator.remove();
            }
        }

        if (shouldRecordRepeatsAsScenarios) {
            this.createScenarios(stubMappings, requestCounts);
        }
    }

    private void createScenarios(Iterable<StubMapping> stubMappings, MultiSet<RequestPattern> requestCounts) {
        final Map<RequestPattern, ScenarioDetails> tracker = new HashMap<>();

        for (StubMapping stubMapping : stubMappings) {
            if (requestCounts.getCount(stubMapping.getRequest()) == 1) {
                continue; // not a repeated request
            }

            if (!tracker.containsKey(stubMapping.getRequest())) {
                tracker.put(stubMapping.getRequest(), new ScenarioDetails(stubMapping.getRequest()));
            }

            tracker.get(stubMapping.getRequest()).updateStubMapping(stubMapping);
        }
    }

    /**
     * Simple container class for building scenarios. Tracks the number of times a request has been seen, the associated
     * scenario name, and the current scenario state;
     */
    private static class ScenarioDetails {
        private final String name;
        private int count = 1;
        private String currentState = Scenario.STARTED;

        private ScenarioDetails(RequestPattern request) {
            this.name = SCENARIO_NAME_PREFIX + "-" + Urls.urlToPathParts(URI.create(request.getUrl()));
        }

        private void updateStubMapping(StubMapping stubMapping) {
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
