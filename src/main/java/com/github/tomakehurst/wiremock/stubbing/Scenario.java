/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;

public class Scenario {

	public static final String STARTED = "Started";

	private final UUID id;
	private final String name;
	private final String state;
	private final Set<String> possibleStates;

	@JsonCreator
	public Scenario(@JsonProperty("id") UUID id,
                    @JsonProperty("name") String name,
                    @JsonProperty("state") String currentState,
                    @JsonProperty("possibleStates") Set<String> possibleStates) {
        this.id = id;
        this.name = name;
        this.state = currentState;
		this.possibleStates = possibleStates;
	}
	
	public static Scenario inStartedState(String name) {
		return new Scenario(UUID.randomUUID(), name, STARTED, ImmutableSet.of(STARTED));
	}

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
		return state;
	}

    public Set<String> getPossibleStates() {
        return possibleStates;
    }

	Scenario setState(String newState) {
		return new Scenario(id, name, newState, possibleStates);
	}

	Scenario reset() {
        return new Scenario(id, name, STARTED, possibleStates);
	}

    Scenario withPossibleState(String newScenarioState) {
	    if (newScenarioState == null) {
	        return this;
        }

        ImmutableSet<String> newStates = ImmutableSet.<String>builder()
            .addAll(possibleStates)
            .add(newScenarioState)
            .build();
        return new Scenario(id, name, state, newStates);
    }

    public Scenario withoutPossibleState(String scenarioState) {
        return new Scenario(
            id,
            name,
            state,
            from(possibleStates)
                .filter(not(equalTo(scenarioState)))
                .toSet()
        );
    }

	@Override
	public String toString() {
		return Json.write(this);
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return Objects.equals(name, scenario.name) &&
            Objects.equals(state, scenario.state) &&
            Objects.equals(possibleStates, scenario.possibleStates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state, possibleStates);
    }

    public static final Predicate<Scenario> withName(final String name) {
	    return new Predicate<Scenario>() {
            @Override
            public boolean apply(Scenario input) {
                return input.getName().equals(name);
            }
        };
    }
}
