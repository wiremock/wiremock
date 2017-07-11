package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotScenarioBuilder;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SnapshotScenarioBuilderTest {
    @Test
    public void addToScenarioWithSingleRequestSeries() {
        StubMapping mapping1 = stubMappingForUrl("/foo/bar");
        StubMapping mapping2 = stubMappingForUrl("/foo/bar");
        StubMapping mapping3 = stubMappingForUrl("/foo/bar");
        SnapshotScenarioBuilder builder = new SnapshotScenarioBuilder();

        builder.addToScenario(mapping1);
        assertEquals("scenario-foo-bar", mapping1.getScenarioName());
        assertEquals(Scenario.STARTED, mapping1.getRequiredScenarioState());
        assertNull(mapping1.getNewScenarioState());

        builder.addToScenario(mapping2);
        assertEquals(mapping1.getScenarioName(), mapping2.getScenarioName());
        assertEquals(Scenario.STARTED, mapping2.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-2", mapping2.getNewScenarioState());

        builder.addToScenario(mapping3);
        assertEquals(mapping1.getScenarioName(), mapping3.getScenarioName());
        assertEquals(mapping2.getNewScenarioState(), mapping3.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-3", mapping3.getNewScenarioState());
    }

    @Test
    public void addToScenarioWithMultipleRequestSeries() {
        StubMapping mapping1 = stubMappingForUrl("/foo/bar");
        StubMapping mapping2 = stubMappingForUrl("/foo");
        StubMapping mapping3 = stubMappingForUrl("/foo/bar");

        SnapshotScenarioBuilder builder = new SnapshotScenarioBuilder();

        builder.addToScenario(mapping1);
        assertEquals("scenario-foo-bar", mapping1.getScenarioName());
        assertEquals(Scenario.STARTED, mapping1.getRequiredScenarioState());
        assertNull(mapping1.getNewScenarioState());

        builder.addToScenario(mapping2);
        assertEquals("scenario-foo", mapping2.getScenarioName());
        assertEquals(Scenario.STARTED, mapping2.getRequiredScenarioState());
        assertNull(mapping2.getNewScenarioState());

        builder.addToScenario(mapping3);
        assertEquals(mapping1.getScenarioName(), mapping3.getScenarioName());
        assertEquals(Scenario.STARTED, mapping3.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-2", mapping3.getNewScenarioState());
    }

    private StubMapping stubMappingForUrl(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
