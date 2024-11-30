/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.NumberHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.TemplateModelDataProviderExtension;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.SystemValueHelper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TemplateEngine {

  private final Handlebars handlebars;
  private final Cache<Object, HandlebarsOptimizedTemplate> cache;
  private final Long maxCacheEntries;

  private final List<TemplateModelDataProviderExtension> templateModelDataProviders;

  public static TemplateEngine defaultTemplateEngine() {
    return new TemplateEngine(emptyMap(), null, null, false, emptyList());
  }

  public TemplateEngine(
      Map<String, Helper<?>> helpers,
      Long maxCacheEntries,
      Set<String> permittedSystemKeys,
      boolean escapingDisabled,
      List<TemplateModelDataProviderExtension> templateModelDataProviders) {

    this.handlebars =
        escapingDisabled ? new Handlebars().with(EscapingStrategy.NOOP) : new Handlebars();

    this.maxCacheEntries = maxCacheEntries;
    this.templateModelDataProviders = templateModelDataProviders;
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    if (maxCacheEntries != null) {
      cacheBuilder.maximumSize(maxCacheEntries);
    }
    cache = cacheBuilder.build();

    addHelpers(helpers, permittedSystemKeys);
  }

  protected TemplateEngine() {
    this.handlebars = null;
    this.maxCacheEntries = null;
    this.cache = null;
    this.templateModelDataProviders = emptyList();
  }

  private void addHelpers(Map<String, Helper<?>> helpers, Set<String> permittedSystemKeys) {
    for (StringHelpers helper : StringHelpers.values()) {
      if (!helper.name().equals("now")) {
        this.handlebars.registerHelper(helper.name(), helper);
      }
    }

    for (NumberHelper helper : NumberHelper.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    for (ConditionalHelpers helper : ConditionalHelpers.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    this.handlebars.registerHelper(AssignHelper.NAME, new AssignHelper());

    // Add all available wiremock helpers
    for (WireMockHelpers helper : WireMockHelpers.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    this.handlebars.registerHelper(
        "systemValue", new SystemValueHelper(new SystemKeyAuthoriser(permittedSystemKeys)));

    for (Map.Entry<String, Helper<?>> entry : helpers.entrySet()) {
      this.handlebars.registerHelper(entry.getKey(), entry.getValue());
    }
  }

  public HandlebarsOptimizedTemplate getTemplate(final Object key, final String content) {
    if (maxCacheEntries != null && maxCacheEntries < 1) {
      return getUncachedTemplate(content);
    }

    try {
      return cache.get(key, () -> new HandlebarsOptimizedTemplate(handlebars, content));
    } catch (ExecutionException e) {
      return Exceptions.throwUnchecked(e, HandlebarsOptimizedTemplate.class);
    }
  }

  public HandlebarsOptimizedTemplate getUncachedTemplate(final String content) {
    return new HandlebarsOptimizedTemplate(handlebars, content);
  }

  public Map<String, Object> buildModelForRequest(ServeEvent serveEvent) {
    final ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
    final Parameters parameters =
        getFirstNonNull(responseDefinition.getTransformerParameters(), Parameters.empty());

    final Map<String, Object> additionalModelData =
        templateModelDataProviders.stream()
            .map(provider -> provider.provideTemplateModelData(serveEvent).entrySet())
            .flatMap(Set::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    final Map<String, Object> model = new HashMap<>();
    model.put("parameters", parameters);
    model.put("request", buildRequestModel(serveEvent.getRequest()));
    if (serveEvent.getResponse() != null) {
      model.put("response", buildResponseModel(serveEvent.getResponse()));
    }
    model.putAll(additionalModelData);
    return model;
  }

  public Map<String, Object> buildModelForRequest(Request request) {
    final Map<String, Object> model = new HashMap<>();
    model.put("request", buildRequestModel(request));
    return model;
  }

  private static RequestTemplateModel buildRequestModel(Request request) {
    RequestLine requestLine = RequestLine.fromRequest(request);
    Map<String, ListOrSingle<String>> adaptedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    adaptedHeaders.putAll(
        Maps.toMap(
            request.getAllHeaderKeys(), input -> ListOrSingle.of(request.header(input).values())));
    Map<String, ListOrSingle<String>> adaptedCookies =
        Maps.transformValues(request.getCookies(), cookie -> ListOrSingle.of(cookie.getValues()));

    return new RequestTemplateModel(
        request.getId() != null ? request.getId().toString() : null,
        requestLine,
        adaptedHeaders,
        adaptedCookies,
        request.isMultipart(),
        Body.ofBinaryOrText(request.getBody(), request.contentTypeHeader()),
        buildRequestPartModel(request));
  }

  private static Map<String, RequestPartTemplateModel> buildRequestPartModel(Request request) {

    if (request.isMultipart()) {
      return request.getParts().stream()
          .collect(
              Collectors.toMap(
                  Request.Part::getName,
                  part ->
                      new RequestPartTemplateModel(
                          part.getName(),
                          part.getHeaders().all().stream()
                              .collect(
                                  Collectors.toMap(
                                      HttpHeader::key, header -> ListOrSingle.of(header.values()))),
                          part.getBody())));
    }

    return Collections.emptyMap();
  }

  private static ResponseTemplateModel buildResponseModel(LoggedResponse response) {
    Map<String, ListOrSingle<String>> adaptedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (response.getHeaders() != null) {
      adaptedHeaders.putAll(
          Maps.toMap(
              response.getHeaders().keys(),
              input -> ListOrSingle.of(response.getHeaders().getHeader(input).values())));
    }
    return new ResponseTemplateModel(
        adaptedHeaders,
        Body.ofBinaryOrText(response.getBody(), response.getHeaders().getContentTypeHeader()));
  }

  public long getCacheSize() {
    return cache.size();
  }

  public void invalidateCache() {
    cache.invalidateAll();
  }

  public Long getMaxCacheEntries() {
    return maxCacheEntries;
  }
}
