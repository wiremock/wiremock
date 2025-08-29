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

import static java.util.Arrays.asList;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The type Record spec builder. */
public class RecordSpecBuilder {

  private String targetBaseUrl;
  private RequestPatternBuilder filterRequestPatternBuilder;
  private List<UUID> filterIds;
  private Map<String, CaptureHeadersSpec> headers = new LinkedHashMap<>();
  private RequestBodyPatternFactory requestBodyPatternFactory;
  private long maxTextBodySize = ResponseDefinitionBodyMatcher.DEFAULT_MAX_TEXT_SIZE;
  private long maxBinaryBodySize = ResponseDefinitionBodyMatcher.DEFAULT_MAX_BINARY_SIZE;
  private boolean persistentStubs = true;
  private boolean repeatsAsScenarios = true;
  private List<String> transformerNames;
  private Parameters transformerParameters;
  private boolean allowNonProxied;

  /**
   * For target record spec builder.
   *
   * @param targetBaseUrl the target base url
   * @return the record spec builder
   */
  public RecordSpecBuilder forTarget(String targetBaseUrl) {
    this.targetBaseUrl = targetBaseUrl;
    return this;
  }

  /**
   * Only requests matching record spec builder.
   *
   * @param filterRequestPattern the filter request pattern
   * @return the record spec builder
   */
  public RecordSpecBuilder onlyRequestsMatching(RequestPatternBuilder filterRequestPattern) {
    this.filterRequestPatternBuilder = filterRequestPattern;
    return this;
  }

  /**
   * Only request ids record spec builder.
   *
   * @param filterIds the filter ids
   * @return the record spec builder
   */
  public RecordSpecBuilder onlyRequestIds(List<UUID> filterIds) {
    this.filterIds = filterIds;
    return this;
  }

  /**
   * Extract text bodies over record spec builder.
   *
   * @param size the size
   * @return the record spec builder
   */
  public RecordSpecBuilder extractTextBodiesOver(long size) {
    this.maxTextBodySize = size;
    return this;
  }

  /**
   * Extract binary bodies over record spec builder.
   *
   * @param size the size
   * @return the record spec builder
   */
  public RecordSpecBuilder extractBinaryBodiesOver(long size) {
    this.maxBinaryBodySize = size;
    return this;
  }

  /**
   * Make stubs persistent record spec builder.
   *
   * @param persistent the persistent
   * @return the record spec builder
   */
  public RecordSpecBuilder makeStubsPersistent(boolean persistent) {
    this.persistentStubs = persistent;
    return this;
  }

  /**
   * Ignore repeat requests record spec builder.
   *
   * @return the record spec builder
   */
  public RecordSpecBuilder ignoreRepeatRequests() {
    this.repeatsAsScenarios = false;
    return this;
  }

  /**
   * Transformers record spec builder.
   *
   * @param transformerName the transformer name
   * @return the record spec builder
   */
  public RecordSpecBuilder transformers(String... transformerName) {
    return transformers(asList(transformerName));
  }

  /**
   * Transformers record spec builder.
   *
   * @param transformerName the transformer name
   * @return the record spec builder
   */
  public RecordSpecBuilder transformers(List<String> transformerName) {
    this.transformerNames = transformerName;
    return this;
  }

  /**
   * Transformer parameters record spec builder.
   *
   * @param parameters the parameters
   * @return the record spec builder
   */
  public RecordSpecBuilder transformerParameters(Parameters parameters) {
    this.transformerParameters = parameters;
    return this;
  }

  /**
   * Capture header record spec builder.
   *
   * @param key the key
   * @return the record spec builder
   */
  public RecordSpecBuilder captureHeader(String key) {
    return captureHeader(key, null);
  }

  /**
   * Capture header record spec builder.
   *
   * @param key the key
   * @param caseInsensitive the case insensitive
   * @return the record spec builder
   */
  public RecordSpecBuilder captureHeader(String key, Boolean caseInsensitive) {
    headers.put(key, new CaptureHeadersSpec(caseInsensitive));
    return this;
  }

  /**
   * Choose body match type automatically record spec builder.
   *
   * @return the record spec builder
   */
  public RecordSpecBuilder chooseBodyMatchTypeAutomatically() {
    return chooseBodyMatchTypeAutomatically(null, null, null);
  }

  /**
   * Choose body match type automatically record spec builder.
   *
   * @param ignoreArrayOrder the ignore array order
   * @param ignoreExtraElements the ignore extra elements
   * @param caseInsensitive the case insensitive
   * @return the record spec builder
   */
  public RecordSpecBuilder chooseBodyMatchTypeAutomatically(
      Boolean ignoreArrayOrder, Boolean ignoreExtraElements, Boolean caseInsensitive) {
    this.requestBodyPatternFactory =
        new RequestBodyAutomaticPatternFactory(
            ignoreArrayOrder, ignoreExtraElements, caseInsensitive);
    return this;
  }

  /**
   * Match request body with equal to json record spec builder.
   *
   * @return the record spec builder
   */
  public RecordSpecBuilder matchRequestBodyWithEqualToJson() {
    return matchRequestBodyWithEqualToJson(null, null);
  }

  /**
   * Match request body with equal to json record spec builder.
   *
   * @param ignoreArrayOrder the ignore array order
   * @param ignoreExtraElements the ignore extra elements
   * @return the record spec builder
   */
  public RecordSpecBuilder matchRequestBodyWithEqualToJson(
      Boolean ignoreArrayOrder, Boolean ignoreExtraElements) {
    this.requestBodyPatternFactory =
        new RequestBodyEqualToJsonPatternFactory(ignoreArrayOrder, ignoreExtraElements);
    return this;
  }

  /**
   * Match request body with equal to xml record spec builder.
   *
   * @return the record spec builder
   */
  public RecordSpecBuilder matchRequestBodyWithEqualToXml() {
    this.requestBodyPatternFactory = new RequestBodyEqualToXmlPatternFactory();
    return this;
  }

  /**
   * Match request body with equal to record spec builder.
   *
   * @return the record spec builder
   */
  public RecordSpecBuilder matchRequestBodyWithEqualTo() {
    return matchRequestBodyWithEqualTo(null);
  }

  /**
   * Match request body with equal to record spec builder.
   *
   * @param caseInsensitive the case insensitive
   * @return the record spec builder
   */
  public RecordSpecBuilder matchRequestBodyWithEqualTo(Boolean caseInsensitive) {
    this.requestBodyPatternFactory = new RequestBodyEqualToPatternFactory(caseInsensitive);
    return this;
  }

  /**
   * Allow non proxied record spec builder.
   *
   * @param allowNonProxied the allow non proxied
   * @return the record spec builder
   */
  public RecordSpecBuilder allowNonProxied(boolean allowNonProxied) {
    this.allowNonProxied = allowNonProxied;
    return this;
  }

  /**
   * Build record spec.
   *
   * @return the record spec
   */
  public RecordSpec build() {
    RequestPattern filterRequestPattern =
        filterRequestPatternBuilder != null ? filterRequestPatternBuilder.build() : null;
    ProxiedServeEventFilters filters =
        filterRequestPatternBuilder != null || filterIds != null || allowNonProxied
            ? new ProxiedServeEventFilters(filterRequestPattern, filterIds, allowNonProxied)
            : null;

    ResponseDefinitionBodyMatcher responseDefinitionBodyMatcher =
        new ResponseDefinitionBodyMatcher(maxTextBodySize, maxBinaryBodySize);

    return new RecordSpec(
        targetBaseUrl,
        filters,
        headers.isEmpty() ? null : headers,
        requestBodyPatternFactory,
        responseDefinitionBodyMatcher,
        SnapshotOutputFormatter.FULL,
        persistentStubs,
        repeatsAsScenarios,
        transformerNames,
        transformerParameters);
  }
}
