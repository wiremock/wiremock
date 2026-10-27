/*
 * Copyright (C) 2017-2026 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.stubbing;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.store.InMemoryScenariosStore;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScenariosTest {

  Scenarios scenarios;

  @BeforeEach
  public void init() {
    scenarios = new InMemoryScenarios();
  }

  @Test
  public void addsANewScenarioWhenStubAddedWithNewScenarioName() {
    StubMapping stub =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();

    scenarios.onStubMappingAdded(stub);

    Scenario scenario = scenarios.getByName("one");

    assertThat(scenario.getState(), is(STARTED));
    assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
  }

  @Test
  public void updatesAnExistingScenarioWhenStubAddedWithExistingScenarioName() {
    StubMapping stub1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(stub1);

    StubMapping stub2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(stub2);

    assertThat(scenarios.getAll().size(), is(1));

    Scenario scenario = scenarios.getByName("one");
    assertThat(scenario.getState(), is(STARTED));
    assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));
  }

  @Test
  public void removesPossibleStateFromScenarioWhenStubThatIsNotTheLastInTheScenarioIsDeleted() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    Scenario scenario = scenarios.getByName("one");
    assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

    scenarios.onStubMappingRemoved(mapping2);

    scenario = scenarios.getByName("one");
    assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2"));
  }

  @Test
  public void removesScenarioCompletelyWhenNoMoreMappingsReferToItDueToRemoval() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    Scenario scenario = scenarios.getByName("one");
    assertThat(scenario.getPossibleStates(), hasItems(STARTED, "step_2", "step_3"));

    scenarios.onStubMappingRemoved(mapping1);
    scenarios.onStubMappingRemoved(mapping2);

    assertThat(scenarios.getAll(), empty());
  }

  @Test
  public void removesScenarioCompletelyWhenNoMoreMappingsReferToItDueToNameChange() {
    StubMapping oldMapping =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(oldMapping);

    assertThat(scenarios.getByName("one"), notNullValue());

    StubMapping newMapping =
        get("/scenarios/1")
            .inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();

    scenarios.onStubMappingUpdated(oldMapping, newMapping);

    assertThat(scenarios.getByName("one"), nullValue());
  }

  @Test
  public void stubMappingCanStopBeingInScenario() {
    StubMapping oldMapping =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(oldMapping);

    assertThat(scenarios.getByName("one"), notNullValue());

    StubMapping newMapping = get("/scenarios/1").willReturn(ok()).build();

    scenarios.onStubMappingUpdated(oldMapping, newMapping);

    assertThat(scenarios.getByName("one"), nullValue());
  }

  @Test
  public void modifiesScenarioStateWhenStubServed() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    assertThat(scenarios.getByName("one").getState(), is(STARTED));

    scenarios.onStubServed(mapping1);
    assertThat(scenarios.getByName("one").getState(), is("step_2"));

    scenarios.onStubServed(mapping2);
    assertThat(scenarios.getByName("one").getState(), is("step_3"));
  }

  @Test
  public void doesNotModifyScenarioStateWhenStubServedInNonMatchingState() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    assertThat(scenarios.getByName("one").getState(), is(STARTED));

    scenarios.onStubServed(mapping2);
    assertThat(scenarios.getByName("one").getState(), is(STARTED));
  }

  @Test
  public void failsWhenStubThatDoesNotChangeStateIsServedForAMissingScenario() {
    StubMapping mapping = get("/scenarios/1").inScenario("missing").willReturn(ok()).build();

    assertThrows(IllegalStateException.class, () -> scenarios.onStubServed(mapping));
  }

  @Test
  public void resetsAllScenarios() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    StubMapping mapping3 =
        get("/scenarios/2")
            .inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping3);

    scenarios.onStubServed(mapping1);
    scenarios.onStubServed(mapping3);

    assertThat(scenarios.getByName("one").getState(), is("step_2"));
    assertThat(scenarios.getByName("two").getState(), is("2_step_2"));

    scenarios.reset();

    assertThat(scenarios.getByName("one").getState(), is(STARTED));
    assertThat(scenarios.getByName("two").getState(), is(STARTED));
  }

  @Test
  public void resetsScenariosInAStoreThatCannotBeModifiedWhileStreamingItsKeys() {
    scenarios = new InMemoryScenarios(new LiveKeysScenariosStore());
    StubMapping mapping =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping);
    scenarios.onStubServed(mapping);

    scenarios.reset();

    assertThat(scenarios.getByName("one").getState(), is(STARTED));
  }

  @Test
  public void clearsScenarios() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    StubMapping mapping3 =
        get("/scenarios/2")
            .inScenario("two")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("2_step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping3);

    assertThat(scenarios.getAll().size(), is(2));

    scenarios.clear();

    assertThat(scenarios.getAll().size(), is(0));
  }

  @Test
  public void checksMappingIsInScenarioState() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step_2")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);

    StubMapping mapping2 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("step_2")
            .willSetStateTo("step_3")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping2);

    assertThat(scenarios.snapshot().mappingMatchesScenarioState(mapping1), is(true));
    assertThat(scenarios.snapshot().mappingMatchesScenarioState(mapping2), is(false));
  }

  @Test
  public void returnsOnlyStartedStateWhenNoNextStateSpecified() {
    StubMapping mapping =
        get("/scenarios/1").inScenario("one").whenScenarioStateIs(STARTED).willReturn(ok()).build();
    scenarios.onStubMappingAdded(mapping);

    Scenario scenario = scenarios.getByName("one");

    assertThat(scenario.getState(), is(STARTED));
    assertThat(scenario.getPossibleStates(), hasItems(STARTED));
  }

  @Test
  public void doesNotAddDuplicatePossibleStates() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
    StubMapping mapping2 =
        get("/scenarios/2")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);
    scenarios.onStubMappingAdded(mapping1);

    Set<String> possibleStates = scenarios.getByName("one").getPossibleStates();
    assertThat(possibleStates.size(), is(2));
    assertThat(possibleStates, hasItems("Started", "step two"));
  }

  @Test
  public void supportsNewScenarioStateWhenRequiredStateIsNull() {
    StubMapping mapping =
        get("/scenarios/1").inScenario("one").willSetStateTo("step two").willReturn(ok()).build();

    scenarios.onStubMappingAdded(mapping);

    scenarios.onStubServed(mapping);

    assertThat(scenarios.getByName("one").getState(), is("step two"));
  }

  @Test
  public void doesNotRemovePossibleStateWhenStubIsRemovedButOtherStubsHaveThatState() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
    StubMapping mapping2 =
        get("/scenarios/2")
            .inScenario("one")
            .whenScenarioStateIs("step two")
            .willSetStateTo("step two")
            .willReturn(ok())
            .build();
    StubMapping mapping3 =
        get("/scenarios/3")
            .inScenario("one")
            .whenScenarioStateIs("step two")
            .willReturn(ok())
            .build();
    scenarios.onStubMappingAdded(mapping1);
    scenarios.onStubMappingAdded(mapping2);
    scenarios.onStubMappingAdded(mapping3);

    scenarios.onStubMappingRemoved(mapping2);

    Set<String> possibleStates = scenarios.getByName("one").getPossibleStates();
    assertThat(possibleStates, hasItems("Started", "step two"));
    assertThat(possibleStates.size(), is(2));
  }

  @Test
  public void returnsAllPossibleScenarioStates() {
    StubMapping mapping1 =
        get("/scenarios/1")
            .inScenario("one")
            .whenScenarioStateIs("A")
            .willSetStateTo("B")
            .willReturn(ok())
            .build();
    StubMapping mapping2 =
        get("/scenarios/1").inScenario("one").willSetStateTo("C").willReturn(ok()).build();
    StubMapping mapping3 =
        get("/scenarios/1").inScenario("one").whenScenarioStateIs("D").willReturn(ok()).build();

    scenarios.onStubMappingAdded(mapping1);
    scenarios.onStubMappingAdded(mapping2);
    scenarios.onStubMappingAdded(mapping3);

    Set<String> possibleStates = scenarios.getByName("one").getPossibleStates();
    assertThat(possibleStates, hasItems("A", "B", "C", "D"));
    assertThat(possibleStates.size(), is(4));
  }

  @Test
  public void doesNotLoseAStateChangeWhenAStubIsAddedToTheScenarioConcurrently() {
    StubMapping toOne =
        get("/advance")
            .inScenario("one")
            .whenScenarioStateIs(STARTED)
            .willSetStateTo("ONE")
            .willReturn(ok())
            .build();
    StubMapping toStarted =
        get("/advance")
            .inScenario("one")
            .whenScenarioStateIs("ONE")
            .willSetStateTo(STARTED)
            .willReturn(ok())
            .build();
    StubMapping read = get("/read").inScenario("one").willReturn(ok()).build();
    scenarios.onStubMappingAdded(toOne);
    scenarios.onStubMappingAdded(toStarted);

    AtomicBoolean stop = new AtomicBoolean(false);
    CompletableFuture<Void> adder =
        CompletableFuture.runAsync(
            () -> {
              while (!stop.get()) {
                scenarios.onStubMappingAdded(read);
              }
            });

    try {
      List<Transition> lostStateChanges =
          IntStream.rangeClosed(1, 100_000)
              .mapToObj(
                  round -> {
                    StubMapping advance =
                        scenarios.getByName("one").getState().equals(STARTED) ? toOne : toStarted;
                    scenarios.onStubServed(advance);
                    return new Transition(
                        round,
                        advance.getNewScenarioState(),
                        scenarios.getByName("one").getState());
                  })
              .filter(Transition::lost)
              .collect(toList());
      assertThat(lostStateChanges, is(empty()));
    } finally {
      stop.set(true);
      adder.join();
    }
  }

  private record Transition(int round, String set, String read) {
    boolean lost() {
      return !set.equals(read);
    }
  }

  private static class LiveKeysScenariosStore extends InMemoryScenariosStore {

    private boolean streamingKeys = false;

    @Override
    public Stream<String> getAllKeys() {
      Iterator<String> keys = super.getAllKeys().iterator();
      Iterator<String> trackingKeys =
          new Iterator<>() {
            @Override
            public boolean hasNext() {
              streamingKeys = keys.hasNext();
              return streamingKeys;
            }

            @Override
            public String next() {
              return keys.next();
            }
          };
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(trackingKeys, 0), false);
    }

    @Override
    public void compute(String key, UnaryOperator<Optional<Scenario>> remapping) {
      if (streamingKeys) {
        throw new ConcurrentModificationException();
      }
      super.compute(key, remapping);
    }
  }
}
