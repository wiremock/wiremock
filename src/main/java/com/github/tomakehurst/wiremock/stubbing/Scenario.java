/*
 * Copyright (C) 2012-2022 Thomas Akehurst
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

import static com.google.common.collect.FluentIterable.from;
import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;

public class Scenario {

  public static final String STARTED = "Started";

  private final String id;
  private final String state;
  private final Set<StubMapping> stubMappings;

  @JsonCreator
  public Scenario(
      @JsonProperty("id") String id,
      @JsonProperty("name") String ignored,
      @JsonProperty("state") String currentState,
      @JsonProperty("possibleStates") Set<String> ignored2,
      @JsonProperty("mappings") Set<StubMapping> stubMappings) {
    this.id = id;
    this.state = currentState;
    this.stubMappings = stubMappings;
  }

  private Scenario(String id, String state, Set<StubMapping> stubMappings) {
    this(id, null, state, null, stubMappings);
  }

  public static Scenario inStartedState(String name) {
    return new Scenario(name, STARTED, Collections.emptySet());
  }

  public String getId() {
    return id;
  }

  // For JSON backwards compatibility
  public String getName() {
    return id;
  }

  public String getState() {
    return state;
  }

  public Set<String> getPossibleStates() {
    FluentIterable<String> requiredStates =
        from(stubMappings)
            .transform(
                new Function<StubMapping, String>() {
                  @Override
                  public String apply(StubMapping mapping) {
                    return mapping.getRequiredScenarioState();
                  }
                });

    return from(stubMappings)
        .transform(
            new Function<StubMapping, String>() {
              @Override
              public String apply(StubMapping mapping) {
                return mapping.getNewScenarioState();
              }
            })
        .append(requiredStates)
        .filter(Predicates.notNull())
        .toSet();
  }

  public Set<StubMapping> getMappings() {
    return stubMappings;
  }

  Scenario setState(String newState) {
    if (!getPossibleStates().contains(newState)) {
      throw new InvalidInputException(
          Errors.single(11, "Scenario my-scenario does not support state " + newState));
    }

    return new Scenario(id, newState, stubMappings);
  }

  Scenario reset() {
    return new Scenario(id, STARTED, stubMappings);
  }

  Scenario withStubMapping(StubMapping stubMapping) {
    Set<StubMapping> newMappings =
        ImmutableSet.<StubMapping>builder().addAll(stubMappings).add(stubMapping).build();

    return new Scenario(id, state, newMappings);
  }

  Scenario withoutStubMapping(StubMapping stubMapping) {
    Set<StubMapping> newMappings = stubMappings.stream()
            .filter(stub -> !stub.getId().equals(stubMapping.getId()))
            .collect(toSet());
    return new Scenario(id, state, newMappings);
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
    return Objects.equals(getId(), scenario.getId())
        && Objects.equals(getState(), scenario.getState())
        && Objects.equals(getMappings(), scenario.getMappings());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getState(), getMappings());
  }

  public static final Predicate<Scenario> withName(final String name) {
    return new Predicate<Scenario>() {
      @Override
      public boolean apply(Scenario input) {
        return input.getId().equals(name);
      }
    };
  }
}
