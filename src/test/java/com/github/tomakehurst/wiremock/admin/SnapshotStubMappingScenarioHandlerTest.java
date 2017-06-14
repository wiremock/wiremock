package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingScenarioHandler;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SnapshotStubMappingScenarioHandlerTest {
    @Test
    public void trackSingleStubMapping() {
        StubMapping stubMapping = stubMappingForUrl("");
        new SnapshotStubMappingScenarioHandler().trackStubMapping(stubMapping);

        // Shouldn't have changed anything, since there hasn't been multiple identical requests
        assertNull(stubMapping.getScenarioName());
        assertNull(stubMapping.getNewScenarioState());
        assertNull(stubMapping.getRequiredScenarioState());
    }

    @Test
    public void reset() {
        StubMapping stubMapping = stubMappingForUrl("/foo");
        SnapshotStubMappingScenarioHandler handler = new SnapshotStubMappingScenarioHandler();

        handler.trackStubMapping(stubMapping);
        handler.reset();
        handler.trackStubMapping(stubMapping);

        assertNull(stubMapping.getScenarioName());
    }

    @Test
    public void trackMultipleStubMappings() {
        StubMapping stubMapping1 = stubMappingForUrl("/foo/bar-baz");
        StubMapping stubMapping2 = stubMappingForUrl("/foo/bar-baz");
        StubMapping stubMapping3 = stubMappingForUrl("/foo/bar-baz");
        SnapshotStubMappingScenarioHandler handler = new SnapshotStubMappingScenarioHandler();

        handler.trackStubMapping(stubMapping1);
        handler.trackStubMapping(stubMappingForUrl("this stub mapping should be ignored"));
        handler.trackStubMapping(stubMapping2);
        handler.trackStubMapping(stubMapping3);

        // Check first stub mapping, which should be the start of the scenario
        assertEquals("scenario-foo-bar-baz", stubMapping1.getScenarioName());
        assertEquals(Scenario.STARTED, stubMapping1.getRequiredScenarioState());
        assertNull(stubMapping1.getNewScenarioState());

        // Check second stub mapping
        assertEquals(stubMapping1.getScenarioName(), stubMapping2.getScenarioName());
        assertEquals(Scenario.STARTED, stubMapping2.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-baz-2", stubMapping2.getNewScenarioState());

        // Check third stub mapping
        assertEquals(stubMapping1.getScenarioName(), stubMapping3.getScenarioName());
        assertEquals("scenario-foo-bar-baz-2", stubMapping3.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-baz-3", stubMapping3.getNewScenarioState());
    }

    private StubMapping stubMappingForUrl(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
