/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase;
import java.util.Objects;
import java.util.Set;
import org.wiremock.annotations.PublishedAPI;

@PublishedAPI
public class MessageServeEventListenerDefinition {

  private final String name;
  private final Set<MessagePhase> phases;
  private final Parameters parameters;

  public MessageServeEventListenerDefinition(String name, Parameters parameters) {
    this(name, null, parameters);
  }

  public MessageServeEventListenerDefinition(
      @JsonProperty("name") String name,
      @JsonProperty("phases") Set<MessagePhase> phases,
      @JsonProperty("parameters") Parameters parameters) {
    this.name = name;
    this.phases = phases;
    this.parameters = parameters;
  }

  public String getName() {
    return name;
  }

  public Set<MessagePhase> getPhases() {
    return phases;
  }

  public Parameters getParameters() {
    return parameters;
  }

  public boolean shouldFireFor(MessagePhase phase) {
    return phases == null || phases.contains(phase);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageServeEventListenerDefinition that = (MessageServeEventListenerDefinition) o;
    return Objects.equals(name, that.name)
        && Objects.equals(phases, that.phases)
        && Objects.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, phases, parameters);
  }
}
