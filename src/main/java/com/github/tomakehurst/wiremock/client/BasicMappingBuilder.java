/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkState;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.extension.ServeEventListenerDefinition;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.*;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class BasicMappingBuilder implements ScenarioMappingBuilder {

  private final RequestPatternBuilder requestPatternBuilder;
  private ResponseDefinitionBuilder responseDefBuilder;
  private Integer priority;
  private String scenarioName;
  private String requiredScenarioState;
  private String newScenarioState;
  private UUID id = UUID.randomUUID();
  private String name;
  private Boolean isPersistent = null;
  private List<PostServeActionDefinition> postServeActions = new ArrayList<>();
  private List<ServeEventListenerDefinition> serveEventListeners = new ArrayList<>();
  private Metadata metadata;

  BasicMappingBuilder(RequestMethod method, UrlPattern urlPattern) {
    requestPatternBuilder = new RequestPatternBuilder(method, urlPattern);
  }

  BasicMappingBuilder(ValueMatcher<Request> requestMatcher) {
    requestPatternBuilder = new RequestPatternBuilder(requestMatcher);
  }

  BasicMappingBuilder(String customRequestMatcherName, Parameters parameters) {
    requestPatternBuilder = new RequestPatternBuilder(customRequestMatcherName, parameters);
  }

  @Override
  public BasicMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder) {
    this.responseDefBuilder = responseDefBuilder;
    return this;
  }

  @Override
  public BasicMappingBuilder atPriority(Integer priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public MappingBuilder withScheme(String scheme) {
    requestPatternBuilder.withScheme(scheme);
    return this;
  }

  @Override
  public MappingBuilder withHost(StringValuePattern hostPattern) {
    requestPatternBuilder.withHost(hostPattern);
    return this;
  }

  @Override
  public MappingBuilder withPort(int port) {
    requestPatternBuilder.withPort(port);
    return this;
  }

  @Override
  public BasicMappingBuilder withHeader(String key, StringValuePattern headerPattern) {
    requestPatternBuilder.withHeader(key, headerPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withHeader(String key, MultiValuePattern headerPattern) {
    requestPatternBuilder.withHeader(key, headerPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern) {
    requestPatternBuilder.withCookie(name, cookieValuePattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern) {
    requestPatternBuilder.withQueryParam(key, queryParamPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withQueryParam(String key, MultiValuePattern queryParamPattern) {
    requestPatternBuilder.withQueryParam(key, queryParamPattern);
    return this;
  }

  @Override
  public ScenarioMappingBuilder withFormParam(String key, StringValuePattern formParamPattern) {
    requestPatternBuilder.withFormParam(key, formParamPattern);
    return this;
  }

  @Override
  public ScenarioMappingBuilder withFormParam(String key, MultiValuePattern formParamPattern) {
    requestPatternBuilder.withFormParam(key, formParamPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withPathParam(String key, StringValuePattern pathParamPattern) {
    requestPatternBuilder.withPathParam(key, pathParamPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withQueryParams(Map<String, StringValuePattern> queryParams) {
    for (Map.Entry<String, StringValuePattern> queryParam : queryParams.entrySet()) {
      requestPatternBuilder.withQueryParam(queryParam.getKey(), queryParam.getValue());
    }
    return this;
  }

  @Override
  public BasicMappingBuilder withRequestBody(ContentPattern<?> bodyPattern) {
    requestPatternBuilder.withRequestBody(bodyPattern);
    return this;
  }

  @Override
  public BasicMappingBuilder withMultipartRequestBody(
      MultipartValuePatternBuilder multipartPatternBuilder) {
    requestPatternBuilder.withRequestBodyPart(multipartPatternBuilder.build());
    return this;
  }

  @Override
  public BasicMappingBuilder inScenario(String scenarioName) {
    checkParameter(scenarioName != null, "Scenario name must not be null");

    this.scenarioName = scenarioName;
    return this;
  }

  @Override
  public BasicMappingBuilder whenScenarioStateIs(String stateName) {
    this.requiredScenarioState = stateName;
    return this;
  }

  @Override
  public BasicMappingBuilder willSetStateTo(String stateName) {
    this.newScenarioState = stateName;
    return this;
  }

  @Override
  public BasicMappingBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public BasicMappingBuilder withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public ScenarioMappingBuilder persistent() {
    this.isPersistent = true;
    return this;
  }

  @Override
  public ScenarioMappingBuilder persistent(boolean persistent) {
    this.isPersistent = persistent;
    return this;
  }

  @Override
  public BasicMappingBuilder withBasicAuth(String username, String password) {
    requestPatternBuilder.withBasicAuth(new BasicCredentials(username, password));
    return this;
  }

  @Override
  public <P> BasicMappingBuilder withPostServeAction(String extensionName, P parameters) {
    postServeActions.add(
        new PostServeActionDefinition(extensionName, resolveParameters(parameters)));
    return this;
  }

  @Override
  public <P> MappingBuilder withServeEventListener(
      Set<ServeEventListener.RequestPhase> requestPhases, String extensionName, P parameters) {
    serveEventListeners.add(
        new ServeEventListenerDefinition(
            extensionName, requestPhases, resolveParameters(parameters)));
    return this;
  }

  @Override
  public <P> MappingBuilder withServeEventListener(String extensionName, P parameters) {
    serveEventListeners.add(
        new ServeEventListenerDefinition(extensionName, resolveParameters(parameters)));
    return this;
  }

  private static <P> Parameters resolveParameters(P parameters) {
    return parameters instanceof Parameters ? (Parameters) parameters : Parameters.of(parameters);
  }

  @Override
  public BasicMappingBuilder withMetadata(Map<String, ?> metadataMap) {
    this.metadata = new Metadata(metadataMap);
    return this;
  }

  @Override
  public BasicMappingBuilder withMetadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @Override
  public BasicMappingBuilder withMetadata(Metadata.Builder metadata) {
    this.metadata = metadata.build();
    return this;
  }

  @Override
  public BasicMappingBuilder andMatching(ValueMatcher<Request> customMatcher) {
    requestPatternBuilder.andMatching(customMatcher);
    return this;
  }

  @Override
  public BasicMappingBuilder andMatching(String customRequestMatcherName) {
    requestPatternBuilder.andMatching(customRequestMatcherName);
    return this;
  }

  @Override
  public BasicMappingBuilder andMatching(String customRequestMatcherName, Parameters parameters) {
    requestPatternBuilder.andMatching(customRequestMatcherName, parameters);
    return this;
  }

  @Override
  public BasicMappingBuilder andMatching(CustomMatcherDefinition matcherDefinition) {
    requestPatternBuilder.andMatching(matcherDefinition);
    return this;
  }

  private boolean requiredScenarioExist() {
    return scenarioName == null && (requiredScenarioState != null || newScenarioState != null);
  }

  @Override
  public StubMapping build() {
    checkState(
        !requiredScenarioExist(),
        "Scenario name must be specified to require or set a new scenario state");
    RequestPattern requestPattern = requestPatternBuilder.build();
    ResponseDefinition response = getFirstNonNull(responseDefBuilder, aResponse()).build();
    StubMapping mapping = new StubMapping(requestPattern, response);
    mapping.setPriority(priority);
    mapping.setScenarioName(scenarioName);
    mapping.setRequiredScenarioState(requiredScenarioState);
    mapping.setNewScenarioState(newScenarioState);
    mapping.setUuid(id);
    mapping.setName(name);
    mapping.setPersistent(isPersistent);

    mapping.setPostServeActions(postServeActions.isEmpty() ? null : postServeActions);
    mapping.setServeEventListenerDefinitions(
        serveEventListeners.isEmpty() ? null : serveEventListeners);

    mapping.setMetadata(metadata);

    return mapping;
  }
}
