/*
 * Copyright (C) 2016-2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResponseTemplateTransformer extends ResponseDefinitionTransformer
    implements StubLifecycleListener {

  public static final String NAME = "response-template";

  private final boolean global;
  private final TemplateEngine templateEngine;

  public static Builder builder() {
    return new Builder();
  }

  public ResponseTemplateTransformer(boolean global) {
    this(global, Collections.emptyMap());
  }

  public ResponseTemplateTransformer(boolean global, String helperName, Helper<?> helper) {
    this(global, ImmutableMap.of(helperName, helper));
  }

  public ResponseTemplateTransformer(boolean global, Map<String, Helper<?>> helpers) {
    this(global, new Handlebars(), helpers, null, null);
  }

  public ResponseTemplateTransformer(
      boolean global,
      Handlebars handlebars,
      Map<String, Helper<?>> helpers,
      Long maxCacheEntries,
      Set<String> permittedSystemKeys) {
    this.global = global;
    this.templateEngine =
        new TemplateEngine(handlebars, helpers, maxCacheEntries, permittedSystemKeys);
  }

  @Override
  public boolean applyGlobally() {
    return global;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ResponseDefinition transform(
      Request request,
      final ResponseDefinition responseDefinition,
      FileSource files,
      Parameters parameters) {
    ResponseDefinitionBuilder newResponseDefBuilder =
        ResponseDefinitionBuilder.like(responseDefinition);

    final ImmutableMap<String, Object> model =
        ImmutableMap.<String, Object>builder()
            .put("parameters", firstNonNull(parameters, Collections.<String, Object>emptyMap()))
            .put("request", RequestTemplateModel.from(request))
            .putAll(addExtraModelElements(request, responseDefinition, files, parameters))
            .build();

    if (responseDefinition.specifiesTextBodyContent()) {
      boolean isJsonBody = responseDefinition.isJsonBody();
      HandlebarsOptimizedTemplate bodyTemplate =
          templateEngine.getTemplate(
              HttpTemplateCacheKey.forInlineBody(responseDefinition),
              responseDefinition.getTextBody());
      applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate, isJsonBody);
    } else if (responseDefinition.specifiesBodyFile()) {
      HandlebarsOptimizedTemplate filePathTemplate =
          templateEngine.getUncachedTemplate(responseDefinition.getBodyFileName());
      String compiledFilePath = uncheckedApplyTemplate(filePathTemplate, model);

      boolean disableBodyFileTemplating = parameters.getBoolean("disableBodyFileTemplating", false);
      if (disableBodyFileTemplating) {
        newResponseDefBuilder.withBodyFile(compiledFilePath);
      } else {
        TextFile file = files.getTextFileNamed(compiledFilePath);
        HandlebarsOptimizedTemplate bodyTemplate =
            templateEngine.getTemplate(
                HttpTemplateCacheKey.forFileBody(responseDefinition, compiledFilePath),
                file.readContentsAsString());
        applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate, false);
      }
    }

    if (responseDefinition.getHeaders() != null) {
      Iterable<HttpHeader> newResponseHeaders =
          Iterables.transform(
              responseDefinition.getHeaders().all(),
              header -> {
                ImmutableList.Builder<String> valueListBuilder = ImmutableList.builder();
                int index = 0;
                for (String headerValue : header.values()) {
                  HandlebarsOptimizedTemplate template =
                      templateEngine.getTemplate(
                          HttpTemplateCacheKey.forHeader(responseDefinition, header.key(), index++),
                          headerValue);
                  valueListBuilder.add(uncheckedApplyTemplate(template, model));
                }

                return new HttpHeader(header.key(), valueListBuilder.build());
              });
      newResponseDefBuilder.withHeaders(new HttpHeaders(newResponseHeaders));
    }

    if (responseDefinition.getProxyBaseUrl() != null) {
      HandlebarsOptimizedTemplate proxyBaseUrlTemplate =
          templateEngine.getTemplate(
              HttpTemplateCacheKey.forProxyUrl(responseDefinition),
              responseDefinition.getProxyBaseUrl());
      String newProxyBaseUrl = uncheckedApplyTemplate(proxyBaseUrlTemplate, model);

      ResponseDefinitionBuilder.ProxyResponseDefinitionBuilder newProxyResponseDefBuilder =
          newResponseDefBuilder.proxiedFrom(newProxyBaseUrl);

      if (responseDefinition.getAdditionalProxyRequestHeaders() != null) {
        Iterable<HttpHeader> newResponseHeaders =
            Iterables.transform(
                responseDefinition.getAdditionalProxyRequestHeaders().all(),
                header -> {
                  ImmutableList.Builder<String> valueListBuilder = ImmutableList.builder();
                  int index = 0;
                  for (String headerValue : header.values()) {
                    HandlebarsOptimizedTemplate template =
                        templateEngine.getTemplate(
                            HttpTemplateCacheKey.forHeader(
                                responseDefinition, header.key(), index++),
                            headerValue);
                    valueListBuilder.add(uncheckedApplyTemplate(template, model));
                  }
                  return new HttpHeader(header.key(), valueListBuilder.build());
                });
        HttpHeaders proxyHttpHeaders = new HttpHeaders(newResponseHeaders);
        for (String key : proxyHttpHeaders.keys()) {
          newProxyResponseDefBuilder.withAdditionalRequestHeader(
              key, proxyHttpHeaders.getHeader(key).firstValue());
        }
      }
      return newProxyResponseDefBuilder.build();
    } else {
      return newResponseDefBuilder.build();
    }
  }

  /** Override this to add extra elements to the template model */
  protected Map<String, Object> addExtraModelElements(
      Request request,
      ResponseDefinition responseDefinition,
      FileSource files,
      Parameters parameters) {
    return Collections.emptyMap();
  }

  private void applyTemplatedResponseBody(
      ResponseDefinitionBuilder newResponseDefBuilder,
      ImmutableMap<String, Object> model,
      HandlebarsOptimizedTemplate bodyTemplate,
      boolean isJsonBody) {
    String newBody = uncheckedApplyTemplate(bodyTemplate, model);
    if (isJsonBody) {
      newResponseDefBuilder.withJsonBody(Json.read(newBody, JsonNode.class));
    } else {
      newResponseDefBuilder.withBody(newBody);
    }
  }

  private String uncheckedApplyTemplate(HandlebarsOptimizedTemplate template, Object context) {
    return template.apply(context);
  }

  @Override
  public void beforeStubCreated(StubMapping stub) {}

  @Override
  public void afterStubCreated(StubMapping stub) {}

  @Override
  public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {}

  @Override
  public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {}

  @Override
  public void beforeStubRemoved(StubMapping stub) {}

  @Override
  public void afterStubRemoved(StubMapping stub) {
    templateEngine.invalidateCache();
  }

  @Override
  public void beforeStubsReset() {}

  @Override
  public void afterStubsReset() {
    templateEngine.invalidateCache();
  }

  public long getCacheSize() {
    return templateEngine.getCacheSize();
  }

  public Long getMaxCacheEntries() {
    return templateEngine.getMaxCacheEntries();
  }

  public static class Builder {
    private boolean global = true;
    private Handlebars handlebars = new Handlebars();
    private Map<String, Helper<?>> helpers = new HashMap<>();
    private Long maxCacheEntries = null;
    private Set<String> permittedSystemKeys = null;

    public Builder global(boolean global) {
      this.global = global;
      return this;
    }

    public Builder handlebars(Handlebars handlebars) {
      this.handlebars = handlebars;
      return this;
    }

    public Builder helpers(Map<String, Helper<?>> helpers) {
      this.helpers = helpers;
      return this;
    }

    public Builder helper(String name, Helper<?> helper) {
      this.helpers.put(name, helper);
      return this;
    }

    public Builder maxCacheEntries(Long maxCacheEntries) {
      this.maxCacheEntries = maxCacheEntries;
      return this;
    }

    public Builder permittedSystemKeys(Set<String> keys) {
      this.permittedSystemKeys = keys;
      return this;
    }

    public Builder permittedSystemKeys(String... keys) {
      this.permittedSystemKeys = ImmutableSet.copyOf(keys);
      return this;
    }

    public ResponseTemplateTransformer build() {
      return new ResponseTemplateTransformer(
          global, handlebars, helpers, maxCacheEntries, permittedSystemKeys);
    }
  }
}
