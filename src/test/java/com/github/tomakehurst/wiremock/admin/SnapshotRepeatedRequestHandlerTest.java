package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotRepeatedRequestHandler;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SnapshotRepeatedRequestHandlerTest {
    @Test
    public void processSingleStubMapping() {
        List<StubMapping> mappings = ImmutableList.of(stubMappingForUrl(""));
        String serialized = Json.write(mappings);
        new SnapshotRepeatedRequestHandler(true).filterOrCreateScenarios(mappings);

        // Shouldn't have changed anything, since there hasn't been multiple identical requests
        assertEquals(serialized, Json.write(mappings));
    }

    @Test
    public void discardsDuplicatesWhenNotUsingScenarios() {
        List<StubMapping> stubMappings = Lists.newArrayList(
            stubMappingForUrl("/dupe"),
            stubMappingForUrl("/dupe"),
            stubMappingForUrl("/different"),
            stubMappingForUrl("/dupe")
        );
        new SnapshotRepeatedRequestHandler(false).filterOrCreateScenarios(stubMappings);

        assertEquals(2, stubMappings.size());

        assertEquals("/dupe", stubMappings.get(0).getRequest().getUrl());
        assertNull(stubMappings.get(0).getScenarioName());
        assertNull(stubMappings.get(0).getRequiredScenarioState());
        assertNull(stubMappings.get(0).getNewScenarioState());

        assertEquals("/different", stubMappings.get(1).getRequest().getUrl());
        assertNull(stubMappings.get(1).getScenarioName());
        assertNull(stubMappings.get(1).getRequiredScenarioState());
        assertNull(stubMappings.get(1).getNewScenarioState());
    }

    @Test
    public void trackMultipleStubMappingsAsScenarios() {
        List<StubMapping> stubMappings = ImmutableList.of(
            stubMappingForUrl("/foo/bar"),
            stubMappingForUrl("/foo/bar"),
            stubMappingForUrl("/not/part/of/scenario"),
            stubMappingForUrl("/foo/bar")
        );

        new SnapshotRepeatedRequestHandler(true).filterOrCreateScenarios(stubMappings);

        assertEquals(4, stubMappings.size());

        // Check first stub mapping, which should be the start of the scenario
        assertEquals("/foo/bar", stubMappings.get(0).getRequest().getUrl());
        assertEquals("scenario-foo-bar", stubMappings.get(0).getScenarioName());
        assertEquals(Scenario.STARTED, stubMappings.get(0).getRequiredScenarioState());
        assertNull(stubMappings.get(0).getNewScenarioState());

        // Check second stub mapping
        assertEquals(stubMappings.get(0).getRequest(), stubMappings.get(1).getRequest());
        assertEquals(stubMappings.get(1).getScenarioName(), stubMappings.get(1).getScenarioName());
        assertEquals(Scenario.STARTED, stubMappings.get(1).getRequiredScenarioState());
        assertEquals("scenario-foo-bar-2", stubMappings.get(1).getNewScenarioState());

        // Check third stub mapping, which is not part of the scenario
        assertEquals("/not/part/of/scenario", stubMappings.get(2).getRequest().getUrl());
        assertNull(stubMappings.get(2).getScenarioName());
        assertNull(stubMappings.get(2).getRequiredScenarioState());
        assertNull(stubMappings.get(2).getNewScenarioState());

        // Check fourth stub mapping, which is the final state in the scenario
        assertEquals(stubMappings.get(0).getRequest(), stubMappings.get(3).getRequest());
        assertEquals(stubMappings.get(3).getScenarioName(), stubMappings.get(3).getScenarioName());
        assertEquals("scenario-foo-bar-2", stubMappings.get(3).getRequiredScenarioState());
        assertEquals("scenario-foo-bar-3", stubMappings.get(3).getNewScenarioState());
    }

    private StubMapping stubMappingForUrl(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
