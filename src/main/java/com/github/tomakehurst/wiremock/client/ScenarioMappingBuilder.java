/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

public interface ScenarioMappingBuilder extends MappingBuilder {

  ScenarioMappingBuilder whenScenarioStateIs(String stateName);

  ScenarioMappingBuilder willSetStateTo(String stateName);

  ScenarioMappingBuilder atPriority(Integer priority);

  ScenarioMappingBuilder withHeader(String key, StringValuePattern headerPattern);

  ScenarioMappingBuilder withHeader(String key, MultiValuePattern headerPattern);

  ScenarioMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern);

  ScenarioMappingBuilder withQueryParam(String key, MultiValuePattern queryParamPattern);

  ScenarioMappingBuilder withFormParam(String key, StringValuePattern formParamPattern);

  ScenarioMappingBuilder withFormParam(String key, MultiValuePattern formParamPattern);

  ScenarioMappingBuilder withQueryParams(Map<String, StringValuePattern> queryParams);

  ScenarioMappingBuilder withRequestBody(ContentPattern<?> bodyPattern);

  ScenarioMappingBuilder withMultipartRequestBody(
      MultipartValuePatternBuilder multipartPatternBuilder);

  ScenarioMappingBuilder inScenario(String scenarioName);

  ScenarioMappingBuilder withId(UUID id);

  ScenarioMappingBuilder persistent();

  ScenarioMappingBuilder persistent(boolean persistent);

  ScenarioMappingBuilder withBasicAuth(String username, String password);

  ScenarioMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern);

  ScenarioMappingBuilder withPostServeAction(String extensionName, Object parameters);

  MappingBuilder withServeEventListener(
      Set<ServeEventListener.RequestPhase> requestPhases, String extensionName, Object parameters);

  MappingBuilder withServeEventListener(String extensionName, Object parameters);

  ScenarioMappingBuilder withMetadata(Map<String, ?> metadata);

  ScenarioMappingBuilder withMetadata(Metadata metadata);

  ScenarioMappingBuilder withMetadata(Metadata.Builder metadata);

  ScenarioMappingBuilder andMatching(ValueMatcher<Request> requestMatcher);

  ScenarioMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder);
}
