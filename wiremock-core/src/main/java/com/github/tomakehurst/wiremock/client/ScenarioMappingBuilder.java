/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.wiremock.annotations.PublishedAPI;

@PublishedAPI
public interface ScenarioMappingBuilder extends MappingBuilder {

  ScenarioMappingBuilder whenScenarioStateIs(String stateName);

  ScenarioMappingBuilder willSetStateTo(String stateName);

  @Override
  ScenarioMappingBuilder atPriority(Integer priority);

  @Override
  ScenarioMappingBuilder withHeader(String key, StringValuePattern headerPattern);

  @Override
  ScenarioMappingBuilder withHeader(String key, MultiValuePattern headerPattern);

  @Override
  ScenarioMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern);

  @Override
  ScenarioMappingBuilder withQueryParam(String key, MultiValuePattern queryParamPattern);

  @Override
  ScenarioMappingBuilder withFormParam(String key, StringValuePattern formParamPattern);

  @Override
  ScenarioMappingBuilder withFormParam(String key, MultiValuePattern formParamPattern);

  @Override
  ScenarioMappingBuilder withFormParams(Map<String, MultiValuePattern> formParams);

  @Override
  ScenarioMappingBuilder withQueryParams(Map<String, StringValuePattern> queryParams);

  @Override
  ScenarioMappingBuilder withRequestBody(ContentPattern<?> bodyPattern);

  @Override
  ScenarioMappingBuilder withMultipartRequestBody(
      MultipartValuePatternBuilder multipartPatternBuilder);

  @Override
  ScenarioMappingBuilder inScenario(String scenarioName);

  @Override
  ScenarioMappingBuilder withId(UUID id);

  @Override
  ScenarioMappingBuilder persistent();

  @Override
  ScenarioMappingBuilder persistent(boolean persistent);

  @Override
  ScenarioMappingBuilder withBasicAuth(String username, String password);

  @Override
  ScenarioMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern);

  @Override
  <P> ScenarioMappingBuilder withPostServeAction(String extensionName, P parameters);

  @Override
  <P> MappingBuilder withServeEventListener(
      Set<ServeEventListener.RequestPhase> requestPhases, String extensionName, P parameters);

  @Override
  <P> MappingBuilder withServeEventListener(String extensionName, P parameters);

  @Override
  ScenarioMappingBuilder withMetadata(Map<String, ?> metadata);

  @Override
  ScenarioMappingBuilder withMetadata(Metadata metadata);

  @Override
  ScenarioMappingBuilder withMetadata(Metadata.Builder metadata);

  @Override
  ScenarioMappingBuilder andMatching(ValueMatcher<Request> requestMatcher);

  @Override
  ScenarioMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder);
}
