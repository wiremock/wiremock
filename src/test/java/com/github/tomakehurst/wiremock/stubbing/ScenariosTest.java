package com.github.tomakehurst.wiremock.stubbing;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ScenariosTest {

    Scenarios scenarios;

    @Before
    public void init() {
        scenarios = new Scenarios();
    }

    @Test
    public void addsANewScenarioWhenStubAddedWithNewScenarioName() {
        scenarios.onStubMappingAddedOrUpdated(
            get("/scenarios/1").inScenario("one")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("step_2")
                .willReturn(ok())
                .build()
        );

        Scenario scenario = scenarios.getByName("one");

        assertThat(scenario.getState(), is(STARTED));
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
    }

    @Test
    public void updatesAnExistingScenarioWhenStubAddedWithExistingScenarioName() {
        scenarios.onStubMappingAddedOrUpdated(
            get("/scenarios/1").inScenario("one")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("step_2")
                .willReturn(ok())
                .build()
        );

        scenarios.onStubMappingAddedOrUpdated(
            get("/scenarios/1").inScenario("one")
                .whenScenarioStateIs("step_2")
                .willSetStateTo("step_3")
                .willReturn(ok())
                .build()
        );

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
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

        Scenario scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

        scenarios.onStubMappingRemoved(mapping2, asList(mapping1, mapping2));

        scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
    }

    @Test
    public void removesScenarioCompletelyWhenNoMoreMappingsReferToIt() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

        Scenario scenario = scenarios.getByName("one");
        assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

        scenarios.onStubMappingRemoved(mapping1, singletonList(mapping2));
        scenarios.onStubMappingRemoved(mapping2, Collections.<StubMapping>emptyList());

        assertThat(scenarios.getAll().values(), empty());
    }

    @Test
    public void modifiesScenarioStateWhenStubServed() {
        StubMapping mapping1 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

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
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

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
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

        StubMapping mapping3 = get("/scenarios/2").inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping3);

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
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

        StubMapping mapping3 = get("/scenarios/2").inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping3);

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
        scenarios.onStubMappingAddedOrUpdated(mapping1);

        StubMapping mapping2 = get("/scenarios/1").inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
        scenarios.onStubMappingAddedOrUpdated(mapping2);

        assertThat(scenarios.mappingMatchesScenarioState(mapping1), is(true));
        assertThat(scenarios.mappingMatchesScenarioState(mapping2), is(false));
    }

    @Test
    public void returnsOnlyStartedStateWhenNoNextStateSpecified() {
        scenarios.onStubMappingAddedOrUpdated(
            get("/scenarios/1").inScenario("one")
                .whenScenarioStateIs(STARTED)
                .willReturn(ok())
                .build()
        );

        Scenario scenario = scenarios.getByName("one");

        assertThat(scenario.getState(), is(STARTED));
        assertThat(scenario.getPossibleStates(), hasItems(STARTED));
    }
}
