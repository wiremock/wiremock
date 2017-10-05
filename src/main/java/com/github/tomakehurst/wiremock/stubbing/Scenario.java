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
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.singletonList;

public class Scenario {

	public static final String STARTED = "Started";
	
	private final String state;
	private final List<String> possibleStates;

	@JsonCreator
	public Scenario(@JsonProperty("state") String currentState,
                    @JsonProperty("possibleStates") List<String> possibleStates) {
		this.state = currentState;
		this.possibleStates = possibleStates;
	}
	
	public static Scenario inStartedState() {
		return new Scenario(STARTED, singletonList(STARTED));
	}
	
	public String getState() {
		return state;
	}

    public List<String> getPossibleStates() {
        return possibleStates;
    }

	Scenario setState(String newState) {
		return new Scenario(newState, possibleStates);
	}

	Scenario reset() {
        return new Scenario(STARTED, possibleStates);
	}

    Scenario withPossibleState(String newScenarioState) {
	    if (newScenarioState == null) {
	        return this;
        }

        ImmutableList<String> newStates = ImmutableList.<String>builder()
            .addAll(possibleStates)
            .add(newScenarioState)
            .build();
        return new Scenario(state, newStates);
    }

    public Scenario withoutPossibleState(String scenarioState) {
        return new Scenario(
            state,
            from(possibleStates).filter(not(equalTo(scenarioState))).toList()
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
        return Objects.equals(state, scenario.state) &&
            Objects.equals(possibleStates, scenario.possibleStates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, possibleStates);
    }
}
