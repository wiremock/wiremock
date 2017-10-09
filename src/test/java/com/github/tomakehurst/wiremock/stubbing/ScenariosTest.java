package com.github.tomakehurst.wiremock.stubbing;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ScenariosTest {

    Scenarios scenarios;

    @Before
    public void init() {
        scenarios = new Scenarios();
    }

    @Test
    public void addsANewScenarioWhenStubAddedWithNewScenarioName() {
        StubMapping stub = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();

        scenarios.onStubMappingAddedOrUpdated(stub, singletonList(stub));

        Scenario scenario = scenarios.getByName("one");

        assertThat(scenario.getState(), is(STARTED));
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
    }

    @Test
    public void updatesAnExistingScenarioWhenStubAddedWithExistingScenarioName() {
        StubMapping stub1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(stub1, singletonList(stub1));

        StubMapping stub2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(stub2, asList(stub1, stub2));

        assertThat(scenarios.getAll().size(), is(1));

        Scenario scenario = scenarios.getByName("one");
        assertThat(scenario.getState(), is(STARTED));
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));
    }

    @Test
    public void removesPossibleStateFromScenarioWhenStubThatIsNotTheLastInTheScenarioIsDeleted() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        Scenario scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

        scenarios.onStubMappingRemoved(mapping2, asList(mapping1, mapping2));

        scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
    }

    @Test
    public void removesScenarioCompletelyWhenNoMoreMappingsReferToItDueToRemoval() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        Scenario scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

        scenarios.onStubMappingRemoved(mapping1, singletonList(mapping2));
        scenarios.onStubMappingRemoved(mapping2, Collections.<StubMapping>emptyList());

        assertThat(scenarios.getAll(), empty());
    }

    @Test
    public void removesScenarioCompletelyWhenNoMoreMappingsReferToItDueToNameChange() {
        StubMapping mapping = get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping, singletonList(mapping));

        assertThat(scenarios.getByName("one"), notNullValue());

        mapping.setScenarioName("two");
        scenarios.onStubMappingAddedOrUpdated(mapping, singletonList(mapping));

        assertThat(scenarios.getByName("one"), nullValue());
    }


    @Test
    public void modifiesScenarioStateWhenStubServed() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        assertThat(scenarios.getByName("one").getState(), is(STARTED));

        scenarios.onStubServed(mapping1);
        assertThat(scenarios.getByName("one").getState(), is("step_2"));

        scenarios.onStubServed(mapping2);
        assertThat(scenarios.getByName("one").getState(), is("step_3"));
    }

    @Test
    public void doesNotModifyScenarioStateWhenStubServedInNonMatchingState() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        assertThat(scenarios.getByName("one").getState(), is(STARTED));

        scenarios.onStubServed(mapping2);
        assertThat(scenarios.getByName("one").getState(), is(STARTED));
    }

    @Test
    public void resetsAllScenarios() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        StubMapping mapping3 = get("/scenarios/2").inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping3, asList(mapping1, mapping2, mapping3));

        scenarios.onStubServed(mapping1);
        scenarios.onStubServed(mapping3);

        assertThat(scenarios.getByName("one").getState(), is("step_2"));
        assertThat(scenarios.getByName("two").getState(), is("2_step_2"));

        scenarios.reset();

        assertThat(scenarios.getByName("one").getState(), is(STARTED));
        assertThat(scenarios.getByName("two").getState(), is(STARTED));
    }

    @Test
    public void clearsScenarios() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        StubMapping mapping3 = get("/scenarios/2").inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping3, asList(mapping1, mapping2, mapping3));

        assertThat(scenarios.getAll().size(), is(2));

        scenarios.clear();

        assertThat(scenarios.getAll().size(), is(0));
    }

    @Test
    public void checksMappingIsInScenarioState() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2, asList(mapping1, mapping2));

        assertThat(scenarios.mappingMatchesScenarioState(mapping1), is(true));
        assertThat(scenarios.mappingMatchesScenarioState(mapping2), is(false));
    }

    @Test
    public void returnsOnlyStartedStateWhenNoNextStateSpecified() {
        StubMapping mapping = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping, singletonList(mapping));

        Scenario scenario = scenarios.getByName("one");

        assertThat(scenario.getState(), is(STARTED));
        assertThat(scenario.getPossibleStates(), hasItems(STARTED));
    }

    @Test
    public void doesNotAddDuplicatePossibleStates() {
        StubMapping mapping1 = get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
        StubMapping mapping2 = get("/scenarios/2")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1, singletonList(mapping1));
        scenarios.onStubMappingAddedOrUpdated(mapping1, asList(mapping1, mapping2));

        Set<String> possibleStates = scenarios.getByName("one").getPossibleStates();
        assertThat(possibleStates.size(), is(2));
        assertThat(possibleStates, hasItems("Started", "step two"));
    }
}
