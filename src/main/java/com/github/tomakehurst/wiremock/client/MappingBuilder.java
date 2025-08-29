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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.CustomMatcherDefinition;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Defines the fluent API for building {@link StubMapping} objects.
 *
 * <p>This interface provides a chainable set of methods to configure a request pattern, the
 * response to be returned, and other metadata for a stub mapping.
 *
 * @see StubMapping
 * @see ScenarioMappingBuilder
 */
public interface MappingBuilder {

  /**
   * Specifies the request scheme to match.
   *
   * @param scheme The scheme (e.g., "https").
   * @return This builder for chaining.
   */
  MappingBuilder withScheme(String scheme);

  /**
   * Specifies the request host pattern to match.
   *
   * @param hostPattern The host pattern.
   * @return This builder for chaining.
   */
  MappingBuilder withHost(StringValuePattern hostPattern);

  /**
   * Specifies the request port to match.
   *
   * @param port The port number.
   * @return This builder for chaining.
   */
  MappingBuilder withPort(int port);

  /**
   * Specifies the client IP address to match.
   *
   * @param hostPattern A pattern for the client IP address.
   * @return This builder for chaining.
   */
  MappingBuilder withClientIp(StringValuePattern hostPattern);

  /**
   * Sets the priority of the stub mapping.
   *
   * <p>Higher priority stubs are matched first.
   *
   * @param priority The priority value.
   * @return This builder for chaining.
   */
  MappingBuilder atPriority(Integer priority);

  /**
   * Adds a request header pattern to match.
   *
   * @param key The name of the header.
   * @param headerPattern The pattern to match against the header's value.
   * @return This builder for chaining.
   */
  MappingBuilder withHeader(String key, StringValuePattern headerPattern);

  /**
   * Adds a request header pattern to match against multiple values.
   *
   * @param key The name of the header.
   * @param headerPattern The pattern to match against the header's values.
   * @return This builder for chaining.
   */
  MappingBuilder withHeader(String key, MultiValuePattern headerPattern);

  /**
   * Adds a path parameter pattern to match.
   *
   * @param name The name of the path parameter.
   * @param pattern The pattern to match against the path parameter's value.
   * @return This builder for chaining.
   */
  MappingBuilder withPathParam(String name, StringValuePattern pattern);

  /**
   * Adds a query parameter pattern to match.
   *
   * @param key The name of the query parameter.
   * @param queryParamPattern The pattern to match against the query parameter's value.
   * @return This builder for chaining.
   */
  MappingBuilder withQueryParam(String key, StringValuePattern queryParamPattern);

  /**
   * Adds a query parameter pattern to match against multiple values.
   *
   * @param key The name of the query parameter.
   * @param multiValueQueryParamPattern The pattern to match against the query parameter's values.
   * @return This builder for chaining.
   */
  MappingBuilder withQueryParam(String key, MultiValuePattern multiValueQueryParamPattern);

  /**
   * Adds a form parameter pattern to match.
   *
   * @param key The name of the form parameter.
   * @param formParamPattern The pattern to match against the form parameter's value.
   * @return This builder for chaining.
   */
  MappingBuilder withFormParam(String key, StringValuePattern formParamPattern);

  /**
   * Adds multiple form parameter patterns to match.
   *
   * @param formParams A map of form parameter names to patterns.
   * @return This builder for chaining.
   */
  MappingBuilder withFormParams(Map<String, MultiValuePattern> formParams);

  /**
   * Adds multiple query parameter patterns to match.
   *
   * @param queryParams A map of query parameter names to patterns.
   * @return This builder for chaining.
   */
  MappingBuilder withQueryParams(Map<String, StringValuePattern> queryParams);

  /**
   * Specifies a pattern to match against the request body.
   *
   * @param bodyPattern The body pattern.
   * @return This builder for chaining.
   */
  MappingBuilder withRequestBody(ContentPattern<?> bodyPattern);

  /**
   * Specifies a pattern to match against a multipart request body.
   *
   * @param multipartPatternBuilder The builder for the multipart pattern.
   * @return This builder for chaining.
   */
  MappingBuilder withMultipartRequestBody(MultipartValuePatternBuilder multipartPatternBuilder);

  /**
   * Associates this stub mapping with a scenario.
   *
   * @param scenarioName The name of the scenario.
   * @return A {@link ScenarioMappingBuilder} for further scenario-specific configuration.
   */
  ScenarioMappingBuilder inScenario(String scenarioName);

  /**
   * Sets the ID of the stub mapping.
   *
   * @param id The UUID of the stub mapping.
   * @return This builder for chaining.
   */
  MappingBuilder withId(UUID id);

  /**
   * Sets a friendly name for the stub mapping.
   *
   * @param name The name of the stub mapping.
   * @return This builder for chaining.
   */
  MappingBuilder withName(String name);

  /**
   * Marks the stub mapping as persistent.
   *
   * <p>Persistent stubs will survive a {@code WireMock.reset()} and can be saved with {@code
   * WireMock.saveAllMappings()}.
   *
   * @return This builder for chaining.
   */
  MappingBuilder persistent();

  /**
   * Sets the persistence status of the stub mapping.
   *
   * @param persistent True if the stub should be persistent, false otherwise.
   * @return This builder for chaining.
   */
  MappingBuilder persistent(boolean persistent);

  /**
   * Adds a basic authentication pattern to match against the {@code Authorization} header.
   *
   * @param username The username.
   * @param password The password.
   * @return This builder for chaining.
   */
  MappingBuilder withBasicAuth(String username, String password);

  /**
   * Adds a cookie pattern to match.
   *
   * @param name The name of the cookie.
   * @param cookieValuePattern The pattern to match against the cookie's value.
   * @return This builder for chaining.
   */
  MappingBuilder withCookie(String name, StringValuePattern cookieValuePattern);

  /**
   * Attaches a post-serve action extension to this stub.
   *
   * @param extensionName The name of the extension.
   * @param parameters Parameters for the extension.
   * @param <P> The type of the parameters object.
   * @return This builder for chaining.
   */
  <P> MappingBuilder withPostServeAction(String extensionName, P parameters);

  /**
   * Attaches a serve event listener extension to this stub for a single request phase.
   *
   * @param requestPhase The request phase to listen to.
   * @param extensionName The name of the extension.
   * @param parameters Parameters for the extension.
   * @param <P> The type of the parameters object.
   * @return This builder for chaining.
   */
  default <P> MappingBuilder withServeEventListener(
      ServeEventListener.RequestPhase requestPhase, String extensionName, P parameters) {
    return withServeEventListener(Set.of(requestPhase), extensionName, parameters);
  }

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
  MappingBuilder withMetadata(Map<String, ?> metadata);

  /**
   * Attaches metadata to this stub mapping using a builder.
   *
   * @param metadata The {@link Metadata.Builder} object.
   * @return This builder for chaining.
   */
  MappingBuilder withMetadata(Metadata.Builder metadata);

  /**
   * Adds a custom request matcher.
   *
   * @param requestMatcher The custom matcher instance.
   * @return This builder for chaining.
   */
  MappingBuilder andMatching(ValueMatcher<Request> requestMatcher);

  /**
   * Adds a named custom request matcher from an extension.
   *
   * @param customRequestMatcherName The name of the custom matcher.
   * @return This builder for chaining.
   */
  MappingBuilder andMatching(String customRequestMatcherName);

  /**
   * Adds a named custom request matcher from an extension with parameters.
   *
   * @param customRequestMatcherName The name of the custom matcher.
   * @param parameters Parameters for the custom matcher.
   * @return This builder for chaining.
   */
  MappingBuilder andMatching(String customRequestMatcherName, Parameters parameters);

  /**
   * Adds a custom request matcher definition.
   *
   * @param matcherDefinition The custom matcher definition.
   * @return This builder for chaining.
   */
  MappingBuilder andMatching(CustomMatcherDefinition matcherDefinition);

  /**
   * Specifies the response to be returned when the request is matched.
   *
   * @param responseDefBuilder The response definition builder.
   * @return This builder for chaining.
   */
  MappingBuilder willReturn(ResponseDefinitionBuilder responseDefBuilder);

  /**
   * Builds the final, configured {@link StubMapping} instance.
   *
   * @return A new {@code StubMapping} instance.
   */
  StubMapping build();
}
