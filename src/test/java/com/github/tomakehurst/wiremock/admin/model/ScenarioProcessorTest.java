package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ScenarioProcessorTest {

    ScenarioProcessor processor = new ScenarioProcessor();

    @Test
    public void placesStubMappingsIntoScenariosWhenRepetitionsArePresent() {
        StubMapping foobar1 = stubMappingForUrl("/foo/bar");
        StubMapping other1 = stubMappingForUrl("/other");
        StubMapping foobar2 = stubMappingForUrl("/foo/bar");
        StubMapping foobar3 = stubMappingForUrl("/foo/bar");
        StubMapping other2 = stubMappingForUrl("/other");

        processor.putRepeatedRequestsInScenarios(asList(foobar1, other1, foobar2, foobar3, other2));

        assertEquals("scenario-foo-bar", foobar1.getScenarioName());
        assertEquals(Scenario.STARTED, foobar1.getRequiredScenarioState());
        assertEquals(foobar1.getNewScenarioState(), "scenario-foo-bar-2");

        assertEquals(foobar1.getScenarioName(), foobar2.getScenarioName());
        assertEquals("scenario-foo-bar-2", foobar2.getRequiredScenarioState());
        assertEquals("scenario-foo-bar-3", foobar2.getNewScenarioState());

        assertEquals(foobar1.getScenarioName(), foobar3.getScenarioName());
        assertEquals("scenario-foo-bar-3", foobar3.getRequiredScenarioState());
        assertNull("Last mapping should not have a state transition", foobar3.getNewScenarioState());

        assertEquals("scenario-other", other1.getScenarioName());
        assertEquals("scenario-other-2", other1.getNewScenarioState());
        assertEquals("scenario-other-2", other2.getRequiredScenarioState());
    }

    private StubMapping stubMappingForUrl(String url) {
        return new StubMapping(
            newRequestPattern().withUrl(url).build(),
            ResponseDefinition.ok()
        );
    }
}
