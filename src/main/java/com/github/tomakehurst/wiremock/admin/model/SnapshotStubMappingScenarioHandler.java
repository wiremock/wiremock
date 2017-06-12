package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.net.URI;

import static java.lang.Math.min;

/**
 * Tracks stub mappings and set scenario details for duplicate requests
 */
public class SnapshotStubMappingScenarioHandler {
    final private static String SCENARIO_NAME_PREFIX = "scenario";

    private int count = 1;
    private StubMapping previousStubMapping;

    public SnapshotStubMappingScenarioHandler(StubMapping stubMapping) {
        previousStubMapping = stubMapping;
    }

    public void trackStubMapping(StubMapping stubMapping) {
        if (count == 1) {
            String name = generateScenarioName(stubMapping.getRequest());
            // We have multiple identical requests. Go back and make previous stub the start
            previousStubMapping.setScenarioName(name);
            previousStubMapping.setRequiredScenarioState(Scenario.STARTED);
        }

        if (count >= 1) {
            String name = previousStubMapping.getScenarioName();
            stubMapping.setScenarioName(name);
            stubMapping.setNewScenarioState(name + "-" + (count + 1));
            String previousState = previousStubMapping.getRequiredScenarioState();
            stubMapping.setRequiredScenarioState(previousState);
        }

        previousStubMapping = stubMapping;
        count++;
    }

    /**
     * Generates a scenario name from the request. Based on UniqueFilenameGenerator
     *
     * @TODO Use a better name generator
     * @param request
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
}
