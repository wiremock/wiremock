/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.fasterxml.jackson.annotation.*;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.ServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** The type Stub mapping. */
@JsonPropertyOrder({"id", "name", "request", "newRequest", "response", "uuid"})
@JsonIgnoreProperties({"$schema"}) // Allows this to be added as a hint to IDEs like VS Code
public class StubMapping {

  /** The constant DEFAULT_PRIORITY. */
  public static final int DEFAULT_PRIORITY = 5;

  private UUID uuid = UUID.randomUUID();
  private String name;

  private Boolean persistent;

  private RequestPattern request;
  private ResponseDefinition response;
  private Integer priority;
  private String scenarioName;
  private String requiredScenarioState;
  private String newScenarioState;

  private List<PostServeActionDefinition> postServeActions;

  private List<ServeEventListenerDefinition> serveEventListeners;

  private Metadata metadata;

  private long insertionIndex;
  private boolean isDirty = true;

  /**
   * Instantiates a new Stub mapping.
   *
   * @param requestPattern the request pattern
   * @param response the response
   */
  public StubMapping(RequestPattern requestPattern, ResponseDefinition response) {
    setRequest(requestPattern);
    this.response = response;
  }

  /** Instantiates a new Stub mapping. */
  public StubMapping() {
    // Concession to Jackson
  }

  /** The constant NOT_CONFIGURED. */
  public static final StubMapping NOT_CONFIGURED =
      new StubMapping(null, ResponseDefinition.notConfigured());

  /**
   * Build from stub mapping.
   *
   * @param mappingSpecJson the mapping spec json
   * @return the stub mapping
   */
  public static StubMapping buildFrom(String mappingSpecJson) {
    return Json.read(mappingSpecJson, StubMapping.class);
  }

  /**
   * Build json string for string.
   *
   * @param mapping the mapping
   * @return the string
   */
  public static String buildJsonStringFor(StubMapping mapping) {
    return Json.write(mapping);
  }

  /**
   * Gets uuid.
   *
   * @return the uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Sets id.
   *
   * @param uuid the uuid
   */
  public void setId(UUID uuid) {
    this.uuid = uuid;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public UUID getId() {
    return uuid;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets uuid.
   *
   * @param uuid the uuid
   */
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  /**
   * Should be persisted boolean.
   *
   * @return the boolean
   */
  public boolean shouldBePersisted() {
    return persistent != null && persistent;
  }

  /**
   * Is persistent boolean.
   *
   * @return the boolean
   */
  public Boolean isPersistent() {
    return persistent;
  }

  /**
   * Sets persistent.
   *
   * @param persistent the persistent
   */
  public void setPersistent(Boolean persistent) {
    this.persistent = persistent;
  }

  /**
   * Gets request.
   *
   * @return the request
   */
  public RequestPattern getRequest() {
    return getFirstNonNull(request, RequestPattern.ANYTHING);
  }

  /**
   * Gets response.
   *
   * @return the response
   */
  public ResponseDefinition getResponse() {
    return getFirstNonNull(response, ResponseDefinition.ok());
  }

  /**
   * Sets request.
   *
   * @param request the request
   */
  public void setRequest(RequestPattern request) {
    this.request = request;
  }

  /**
   * Sets response.
   *
   * @param response the response
   */
  public void setResponse(ResponseDefinition response) {
    this.response = response;
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  /**
   * Gets insertion index.
   *
   * @return the insertion index
   */
  @JsonView(Json.PrivateView.class)
  public long getInsertionIndex() {
    return insertionIndex;
  }

  /**
   * Sets insertion index.
   *
   * @param insertionIndex the insertion index
   */
  public void setInsertionIndex(long insertionIndex) {
    this.insertionIndex = insertionIndex;
  }

  /**
   * Is dirty boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isDirty() {
    return isDirty;
  }

  /**
   * Sets dirty.
   *
   * @param isDirty the is dirty
   */
  @JsonIgnore
  public void setDirty(boolean isDirty) {
    this.isDirty = isDirty;
  }

  /**
   * Gets priority.
   *
   * @return the priority
   */
  public Integer getPriority() {
    return priority;
  }

  /**
   * Sets priority.
   *
   * @param priority the priority
   */
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  /**
   * Gets scenario name.
   *
   * @return the scenario name
   */
  public String getScenarioName() {
    return scenarioName;
  }

  /**
   * Sets scenario name.
   *
   * @param scenarioName the scenario name
   */
  public void setScenarioName(String scenarioName) {
    this.scenarioName = scenarioName;
  }

  /**
   * Gets required scenario state.
   *
   * @return the required scenario state
   */
  public String getRequiredScenarioState() {
    return requiredScenarioState;
  }

  /**
   * Sets required scenario state.
   *
   * @param requiredScenarioState the required scenario state
   */
  public void setRequiredScenarioState(String requiredScenarioState) {
    this.requiredScenarioState = requiredScenarioState;
  }

  /**
   * Gets new scenario state.
   *
   * @return the new scenario state
   */
  public String getNewScenarioState() {
    return newScenarioState;
  }

  /**
   * Sets new scenario state.
   *
   * @param newScenarioState the new scenario state
   */
  public void setNewScenarioState(String newScenarioState) {
    this.newScenarioState = newScenarioState;
  }

  /**
   * Is in scenario boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isInScenario() {
    return scenarioName != null;
  }

  /**
   * Modifies scenario state boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean modifiesScenarioState() {
    return newScenarioState != null;
  }

  /**
   * Is independent of scenario state boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isIndependentOfScenarioState() {
    return !isInScenario() || requiredScenarioState == null;
  }

  /**
   * Compare priority with int.
   *
   * @param otherMapping the other mapping
   * @return the int
   */
  public int comparePriorityWith(StubMapping otherMapping) {
    int thisPriority = priority != null ? priority : DEFAULT_PRIORITY;
    int otherPriority = otherMapping.priority != null ? otherMapping.priority : DEFAULT_PRIORITY;
    return thisPriority - otherPriority;
  }

  /**
   * Gets post serve actions.
   *
   * @return the post serve actions
   */
  public List<PostServeActionDefinition> getPostServeActions() {
    return postServeActions;
  }

  /**
   * Gets serve event listeners.
   *
   * @return the serve event listeners
   */
  public List<ServeEventListenerDefinition> getServeEventListeners() {
    return serveEventListeners;
  }

  /**
   * Sets post serve actions.
   *
   * @param postServeActions the post serve actions
   */
  public void setPostServeActions(List<PostServeActionDefinition> postServeActions) {
    this.postServeActions = postServeActions;
  }

  /**
   * Sets post serve actions.
   *
   * @param postServeActions the post serve actions
   */
  @SuppressWarnings("unchecked")
  @JsonProperty("postServeActions")
  public void setPostServeActions(Object postServeActions) {
    if (postServeActions == null) {
      return;
    }

    // Ensure backwards compatibility with object/map form
    if (Map.class.isAssignableFrom(postServeActions.getClass())) {
      this.postServeActions =
          ((Map<String, Parameters>) postServeActions)
              .entrySet().stream()
                  .map(
                      entry ->
                          new PostServeActionDefinition(
                              entry.getKey(), Parameters.from(entry.getValue())))
                  .collect(Collectors.toList());
    } else if (List.class.isAssignableFrom(postServeActions.getClass())) {
      this.postServeActions =
          ((List<Map<String, Object>>) postServeActions)
              .stream()
                  .map(item -> Json.mapToObject(item, PostServeActionDefinition.class))
                  .collect(Collectors.toList());
    }
  }

  /**
   * Sets serve event listener definitions.
   *
   * @param serveEventListeners the serve event listeners
   */
  public void setServeEventListenerDefinitions(
      List<ServeEventListenerDefinition> serveEventListeners) {
    this.serveEventListeners = serveEventListeners;
  }

  /**
   * Sets serve event listeners.
   *
   * @param serveEventListeners the serve event listeners
   */
  @SuppressWarnings("unchecked")
  @JsonProperty("serveEventListeners")
  public void setServeEventListeners(Object serveEventListeners) {
    if (serveEventListeners == null) {
      return;
    }

    if (List.class.isAssignableFrom(serveEventListeners.getClass())) {
      this.serveEventListeners =
          ((List<Map<String, Object>>) serveEventListeners)
              .stream()
                  .map(item -> Json.mapToObject(item, ServeEventListenerDefinition.class))
                  .collect(Collectors.toList());
    }
  }

  /**
   * Gets metadata.
   *
   * @return the metadata
   */
  public Metadata getMetadata() {
    return metadata;
  }

  /**
   * Sets metadata.
   *
   * @param metadata the metadata
   */
  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StubMapping that = (StubMapping) o;
    return isDirty == that.isDirty
        && Objects.equals(uuid, that.uuid)
        && Objects.equals(request, that.request)
        && Objects.equals(response, that.response)
        && Objects.equals(priority, that.priority)
        && Objects.equals(scenarioName, that.scenarioName)
        && Objects.equals(requiredScenarioState, that.requiredScenarioState)
        && Objects.equals(newScenarioState, that.newScenarioState)
        && Objects.equals(postServeActions, that.postServeActions)
        && Objects.equals(metadata, that.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        uuid,
        request,
        response,
        priority,
        scenarioName,
        requiredScenarioState,
        newScenarioState,
        postServeActions,
        metadata,
        isDirty);
  }
}
