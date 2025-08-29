/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

/**
 * An extension of {@link MappingBuilder} that adds methods for defining stateful behavior via
 * scenarios.
 *
 * <p>This builder is used to create {@link com.github.tomakehurst.wiremock.stubbing.StubMapping}s
 * that are part of a sequence, where stubs are only active when a scenario is in a specific state.
 *
 * @see MappingBuilder
 * @see com.github.tomakehurst.wiremock.stubbing.Scenario
 */
public interface ScenarioMappingBuilder extends MappingBuilder {

  /**
   * Specifies the required state of a scenario for this stub mapping to be active.
   *
   * @param stateName The name of the required state.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder whenScenarioStateIs(String stateName);

  /**
   * Specifies the new state a scenario will be in after this stub mapping is matched.
   *
   * @param stateName The name of the new state.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder willSetStateTo(String stateName);

  /**
   * Sets the priority of the stub mapping.
   *
   * <p>Higher priority stubs are matched first.
   *
   * @param priority The priority value.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder atPriority(Integer priority);

  /**
   * Adds a request header pattern to match.
   *
   * @param key The name of the header.
   * @param headerPattern The pattern to match against the header's value.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withHeader(String key, StringValuePattern headerPattern);

  /**
   * Adds a request header pattern to match against multiple values.
   *
   * @param key The name of the header.
   * @param headerPattern The pattern to match against the header's values.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withHeader(String key, MultiValuePattern headerPattern);

  /**
   * Adds a request query parameter pattern to match.
   *
   * @param key The name of the query parameter.
   * @param queryParamPattern The pattern to match against the query parameter's value.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern);

  /**
   * Adds a query parameter pattern to match against multiple values.
   *
   * @param key The name of the query parameter.
   * @param queryParamPattern The pattern to match against the query parameter's values.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withQueryParam(String key, MultiValuePattern queryParamPattern);

  /**
   * Adds a request form parameter pattern to match.
   *
   * @param key The name of the form parameter.
   * @param formParamPattern The pattern to match against the form parameter's value.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withFormParam(String key, StringValuePattern formParamPattern);

    ScenarioMappingBuilder withFormParam(String key, MultiValuePattern formParamPattern);

    /**
   * Adds multiple form parameter patterns to match.
   *
   * @param formParams A map of form parameter names to patterns.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withFormParams(Map<String, MultiValuePattern> formParams);

  /**
   * Adds multiple query parameter patterns to match.
   *
   * @param queryParams A map of query parameter names to patterns.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withQueryParams(Map<String, StringValuePattern> queryParams);

  /**
   * Specifies a pattern to match against the request body.
   *
   * @param bodyPattern The body pattern.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withRequestBody(ContentPattern<?> bodyPattern);

  /**
   * Specifies a pattern to match against a multipart request body.
   *
   * @param multipartPatternBuilder The builder for the multipart pattern.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withMultipartRequestBody(
      MultipartValuePatternBuilder multipartPatternBuilder);

  /**
   * Associates this stub mapping with a scenario.
   *
   * @param scenarioName The name of the scenario.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder inScenario(String scenarioName);

  /**
   * Sets the ID of the stub mapping.
   *
   * @param id The UUID of the stub mapping.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withId(UUID id);

  /**
   * Marks the stub mapping as persistent.
   *
   * <p>Persistent stubs will survive a {@code WireMock.reset()} and can be saved with {@code
   * WireMock.saveAllMappings()}.
   *
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder persistent();

  /**
   * Sets the persistence status of the stub mapping.
   *
   * @param persistent True if the stub should be persistent, false otherwise.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder persistent(boolean persistent);

  /**
   * Adds a basic authentication pattern to match against the {@code Authorization} header.
   *
   * @param username The username.
   * @param password The password.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withBasicAuth(String username, String password);

  /**
   * Adds a cookie pattern to match.
   *
   * @param name The name of the cookie.
   * @param cookieValuePattern The pattern to match against the cookie's value.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withCookie(String name, StringValuePattern cookieValuePattern);

  /**
   * Attaches a post-serve action extension to this stub.
   *
   * @param extensionName The name of the extension.
   * @param parameters Parameters for the extension.
   * @param <P> The type of the parameters object.
   * @return This builder for chaining.
   */
  <P> ScenarioMappingBuilder withPostServeAction(String extensionName, P parameters);

  /**
   * Attaches a serve event listener extension to this stub for multiple request phases.
   *
   * @param requestPhases The set of request phases to listen to.
   * @param extensionName The name of the extension.
   * @param parameters Parameters for the extension.
   * @param <P> The type of the parameters object.
   * @return This builder for chaining.
   */
  <P> MappingBuilder withServeEventListener(
      Set<ServeEventListener.RequestPhase> requestPhases, String extensionName, P parameters);

  /**
   * Attaches a serve event listener extension to this stub, listening to all request phases.
   *
   * @param extensionName The name of the extension.
   * @param parameters Parameters for the extension.
   * @param <P> The type of the parameters object.
   * @return This builder for chaining.
   */
  <P> MappingBuilder withServeEventListener(String extensionName, P parameters);

  /**
   * Attaches metadata to this stub mapping.
   *
   * @param metadata A map containing the metadata.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withMetadata(Map<String, ?> metadata);

  BasicMappingBuilder withMetadata(Metadata metadata);

  /**
   * Attaches metadata to this stub mapping using a builder.
   *
   * @param metadata The {@link Metadata.Builder} object.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder withMetadata(Metadata.Builder metadata);

  /**
   * Adds a custom request matcher.
   *
   * @param requestMatcher The custom matcher instance.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder andMatching(ValueMatcher<Request> requestMatcher);

  /**
   * Specifies the response to be returned when the request is matched.
   *
   * @param responseDefBuilder The response definition builder.
   * @return This builder for chaining.
   */
  ScenarioMappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder);
}
