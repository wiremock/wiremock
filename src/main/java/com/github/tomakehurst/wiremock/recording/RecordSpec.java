/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.Parameters;
import java.util.List;
import java.util.Map;

/** Encapsulates options for generating and outputting StubMappings. */
public class RecordSpec {

  // Target URL when using the recording API. Not applicable to snapshotting
  private final String targetBaseUrl;

  // Whitelist requests to generate StubMappings for
  private final ProxiedServeEventFilters filters;

  // Headers from the request to include in the stub mapping, if they match the corresponding
  // matcher
  private final Map<String, CaptureHeadersSpec> captureHeaders;

  // Factory for the StringValuePattern that will be used to match request bodies
  private final RequestBodyPatternFactory requestBodyPatternFactory;

  // Criteria for extracting body from responses
  private final ResponseDefinitionBodyMatcher extractBodyCriteria;

  // How to format StubMappings in the response body
  private final SnapshotOutputFormatter outputFormat;

  // Whether to persist stub mappings
  private final Boolean persist;

  // Whether duplicate requests should be recorded as scenarios or just discarded
  private final Boolean repeatsAsScenarios;

  // Stub mapping transformers
  private final List<String> transformers;

  // Parameters for stub mapping transformers
  private final Parameters transformerParameters;

  /**
   * Instantiates a new Record spec.
   *
   * @param targetBaseUrl the target base url
   * @param filters the filters
   * @param captureHeaders the capture headers
   * @param requestBodyPatternFactory the request body pattern factory
   * @param extractBodyCriteria the extract body criteria
   * @param outputFormat the output format
   * @param persist the persist
   * @param repeatsAsScenarios the repeats as scenarios
   * @param transformers the transformers
   * @param transformerParameters the transformer parameters
   */
  @JsonCreator
  public RecordSpec(
      @JsonProperty("targetBaseUrl") String targetBaseUrl,
      @JsonProperty("filters") ProxiedServeEventFilters filters,
      @JsonProperty("captureHeaders") Map<String, CaptureHeadersSpec> captureHeaders,
      @JsonProperty("requestBodyPattern") RequestBodyPatternFactory requestBodyPatternFactory,
      @JsonProperty("extractBodyCriteria") ResponseDefinitionBodyMatcher extractBodyCriteria,
      @JsonProperty("outputFormat") SnapshotOutputFormatter outputFormat,
      @JsonProperty("persist") Boolean persist,
      @JsonProperty("repeatsAsScenarios") Boolean repeatsAsScenarios,
      @JsonProperty("transformers") List<String> transformers,
      @JsonProperty("transformerParameters") Parameters transformerParameters) {
    this.targetBaseUrl = targetBaseUrl;
    this.filters = filters == null ? ProxiedServeEventFilters.ALLOW_ALL : filters;
    this.captureHeaders = captureHeaders;
    this.requestBodyPatternFactory =
        requestBodyPatternFactory == null
            ? RequestBodyAutomaticPatternFactory.DEFAULTS
            : requestBodyPatternFactory;
    this.extractBodyCriteria = extractBodyCriteria;
    this.outputFormat = outputFormat == null ? SnapshotOutputFormatter.FULL : outputFormat;
    this.persist = persist == null ? true : persist;
    this.repeatsAsScenarios = repeatsAsScenarios;
    this.transformers = transformers;
    this.transformerParameters = transformerParameters;
  }

  private RecordSpec() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  /** The constant DEFAULTS. */
  public static final RecordSpec DEFAULTS = new RecordSpec();

  /**
   * For base url record spec.
   *
   * @param targetBaseUrl the target base url
   * @return the record spec
   */
  public static RecordSpec forBaseUrl(String targetBaseUrl) {
    return new RecordSpec(targetBaseUrl, null, null, null, null, null, null, true, null, null);
  }

  /**
   * Gets target base url.
   *
   * @return the target base url
   */
  public String getTargetBaseUrl() {
    return targetBaseUrl;
  }

  /**
   * Gets filters.
   *
   * @return the filters
   */
  public ProxiedServeEventFilters getFilters() {
    return filters;
  }

  /**
   * Gets capture headers.
   *
   * @return the capture headers
   */
  public Map<String, CaptureHeadersSpec> getCaptureHeaders() {
    return captureHeaders;
  }

  /**
   * Gets output format.
   *
   * @return the output format
   */
  public SnapshotOutputFormatter getOutputFormat() {
    return outputFormat;
  }

  /**
   * Should persist boolean.
   *
   * @return the boolean
   */
  @JsonProperty("persist")
  public boolean shouldPersist() {
    return persist;
  }

  /**
   * Should record repeats as scenarios boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean shouldRecordRepeatsAsScenarios() {
    return repeatsAsScenarios == null ? true : repeatsAsScenarios;
  }

  /**
   * Gets repeats as scenarios.
   *
   * @return the repeats as scenarios
   */
  public Boolean getRepeatsAsScenarios() {
    return repeatsAsScenarios;
  }

  /**
   * Gets transformers.
   *
   * @return the transformers
   */
  public List<String> getTransformers() {
    return transformers;
  }

  /**
   * Gets transformer parameters.
   *
   * @return the transformer parameters
   */
  public Parameters getTransformerParameters() {
    return transformerParameters;
  }

  /**
   * Gets extract body criteria.
   *
   * @return the extract body criteria
   */
  public ResponseDefinitionBodyMatcher getExtractBodyCriteria() {
    return extractBodyCriteria;
  }

  /**
   * Gets request body pattern factory.
   *
   * @return the request body pattern factory
   */
  public RequestBodyPatternFactory getRequestBodyPatternFactory() {
    return requestBodyPatternFactory;
  }
}
