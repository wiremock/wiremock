/*
 * Copyright (C) 2023 Thomas Akehurst
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
import java.util.Set;

public class ServeEventListenerDefinition {

  private final String name;
  private final Set<ServeEventListener.RequestPhase> requestPhases;
  private final Parameters parameters;

  public ServeEventListenerDefinition(String name, Parameters parameters) {
    this(name, null, parameters);
  }

  public ServeEventListenerDefinition(
      @JsonProperty("name") String name,
      @JsonProperty("requestPhases") Set<ServeEventListener.RequestPhase> requestPhases,
      @JsonProperty("parameters") Parameters parameters) {
    this.name = name;
    this.requestPhases = requestPhases;
    this.parameters = parameters;
  }

  public String getName() {
    return name;
  }

  public Set<ServeEventListener.RequestPhase> getRequestPhases() {
    return requestPhases;
  }

  public Parameters getParameters() {
    return parameters;
  }

  public boolean shouldFireFor(ServeEventListener.RequestPhase requestPhase) {
    return requestPhases == null || requestPhases.contains(requestPhase);
  }
}
