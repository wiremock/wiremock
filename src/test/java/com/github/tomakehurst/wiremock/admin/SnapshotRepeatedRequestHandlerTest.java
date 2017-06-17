package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotRepeatedRequestHandler;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SnapshotRepeatedRequestHandlerTest {
    @Test
    public void processSingleStubMapping() {
        List<StubMapping> mappings = Lists.newArrayList(stubMappingForUrl(""));
        List<StubMapping> results = new SnapshotRepeatedRequestHandler(true)
            .processStubMappings(mappings);

        // Shouldn't have changed anything, since there hasn't been multiple identical requests
        assertEquals(mappings, results);
    }

    @Test
    public void resetsAfterRun() {
        List<StubMapping> mappings = Lists.newArrayList(stubMappingForUrl(""));
        SnapshotRepeatedRequestHandler handler = new SnapshotRepeatedRequestHandler(false);

        List<StubMapping> results1 = handler.processStubMappings(mappings);
        // Should not have persisted request tracking state
        List<StubMapping> results2 = handler.processStubMappings(mappings);

        assertEquals(mappings, results1);
        assertEquals(results1, results2);
    }

    @Test
    public void trackMultipleStubMappingsAsScenarios() {
        List<StubMapping> stubMappings = Lists.newArrayList(
            stubMappingForUrl("/foo/bar"),
            stubMappingForUrl("/foo/bar"),
            stubMappingForUrl("/not/part/of/scenario"),
            stubMappingForUrl("/foo/bar")
        );

        SnapshotRepeatedRequestHandler handler = new SnapshotRepeatedRequestHandler(true);
        List<StubMapping> results = handler.processStubMappings(stubMappings);

        assertEquals(4, results.size());

        // Check first stub mapping, which should be the start of the scenario
        assertEquals("/foo/bar", results.get(0).getRequest().getUrl());
        assertEquals("scenario-foo-bar", results.get(0).getScenarioName());
        assertEquals(Scenario.STARTED, results.get(0).getRequiredScenarioState());
        assertNull(results.get(0).getNewScenarioState());

        // Check second stub mapping
        assertEquals(results.get(0).getRequest(), results.get(1).getRequest());
        assertEquals(results.get(1).getScenarioName(), results.get(1).getScenarioName());
        assertEquals(Scenario.STARTED, results.get(1).getRequiredScenarioState());
        assertEquals("scenario-foo-bar-2", results.get(1).getNewScenarioState());

        // Check third stub mapping, which is not part of the scenario
        assertEquals("/not/part/of/scenario", results.get(2).getRequest().getUrl());
        assertNull(results.get(2).getScenarioName());
        assertNull(results.get(2).getRequiredScenarioState());
        assertNull(results.get(2).getNewScenarioState());

        // Check fourth stub mapping, which is the final state in the scenario
        assertEquals(results.get(0).getRequest(), results.get(3).getRequest());
        assertEquals(results.get(3).getScenarioName(), results.get(3).getScenarioName());
        assertEquals("scenario-foo-bar-2", results.get(3).getRequiredScenarioState());
        assertEquals("scenario-foo-bar-3", results.get(3).getNewScenarioState());
    }

    private StubMapping stubMappingForUrl(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
