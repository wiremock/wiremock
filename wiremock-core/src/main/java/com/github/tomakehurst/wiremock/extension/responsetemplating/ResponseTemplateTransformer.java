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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.common.entity.EncodingType.TEXT;
import static com.github.tomakehurst.wiremock.common.entity.TextFormat.JSON;

import com.github.jknack.handlebars.HandlebarsException;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.EntityResolver;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.*;
import java.util.stream.Collectors;

public class ResponseTemplateTransformer
    implements StubLifecycleListener, ResponseDefinitionTransformerV2 {

  public static final String NAME = "response-template";

  private final boolean global;
  private final FileSource files;
  private final EntityResolver entityResolver;
  private final TemplateEngine templateEngine;

  public ResponseTemplateTransformer(
      TemplateEngine templateEngine,
      boolean global,
      FileSource files,
      EntityResolver entityResolver) {
    this.templateEngine = templateEngine;
    this.global = global;
    this.files = files;
    this.entityResolver = entityResolver;
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
  public ResponseDefinition transform(ServeEvent serveEvent) {
    try {
      final Request request = serveEvent.getRequest();
      final ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
      final Parameters parameters = responseDefinition.getTransformerParameters();

      ResponseDefinitionBuilder newResponseDefBuilder =
          ResponseDefinitionBuilder.like(responseDefinition);

      final Map<String, Object> model = templateEngine.buildModelForRequest(serveEvent);
      model.putAll(addExtraModelElements(request, responseDefinition, files, parameters));

      EntityDefinition<?> bodyDefinition = responseDefinition.getBodyEntity();
      HttpTemplateCacheKey templateCacheKey;
      if (bodyDefinition.isFromFile()) {
        HandlebarsOptimizedTemplate filePathTemplate =
            templateEngine.getUncachedTemplate(responseDefinition.getBodyEntity().getFilePath());
        String interpolatedFilePath = uncheckedApplyTemplate(filePathTemplate, model);
        bodyDefinition =
            bodyDefinition.transform(builder -> builder.setFilePath(interpolatedFilePath));
        templateCacheKey =
            HttpTemplateCacheKey.forFileBody(responseDefinition, interpolatedFilePath);
      } else {
        templateCacheKey = HttpTemplateCacheKey.forInlineBody(responseDefinition);
      }

      final Entity initialBody = entityResolver.resolve(bodyDefinition);

      final boolean bodyIsInlineOrTemplatingPermitted =
          bodyDefinition.isInline() || !parameters.getBoolean("disableBodyFileTemplating", false);

      if (bodyIsInlineOrTemplatingPermitted) {
        final HandlebarsOptimizedTemplate bodyTemplate =
            templateEngine.getTemplate(templateCacheKey, initialBody.getDataAsString());

        bodyDefinition = applyTemplateToBodyEntity(model, bodyTemplate);
      }
      newResponseDefBuilder.withBody(bodyDefinition);

      List<HttpHeader> newResponseHeaders =
          responseDefinition.getHeaders().all().stream()
              .map(
                  header -> {
                    ArrayList<String> valueListBuilder = new ArrayList<>();
                    int index = 0;
                    for (String headerValue : header.values()) {
                      HandlebarsOptimizedTemplate template =
                          templateEngine.getTemplate(
                              HttpTemplateCacheKey.forHeader(
                                  responseDefinition, header.key(), index++),
                              headerValue);
                      valueListBuilder.add(uncheckedApplyTemplate(template, model));
                    }

                    return new HttpHeader(header.key(), valueListBuilder);
                  })
              .collect(Collectors.toList());
      newResponseDefBuilder.withHeaders(new HttpHeaders(newResponseHeaders));

      if (responseDefinition.getProxyBaseUrl() != null) {
        HandlebarsOptimizedTemplate proxyBaseUrlTemplate =
            templateEngine.getTemplate(
                HttpTemplateCacheKey.forProxyUrl(responseDefinition),
                responseDefinition.getProxyBaseUrl());
        String newProxyBaseUrl = uncheckedApplyTemplate(proxyBaseUrlTemplate, model);

        ResponseDefinitionBuilder.ProxyResponseDefinitionBuilder newProxyResponseDefBuilder =
            newResponseDefBuilder.proxiedFrom(newProxyBaseUrl);

        List<HttpHeader> newProxyResponseResponseHeaders =
            responseDefinition.getAdditionalProxyRequestHeaders().all().stream()
                .map(
                    header -> {
                      ArrayList<String> valueListBuilder = new ArrayList<>();
                      int index = 0;
                      for (String headerValue : header.values()) {
                        HandlebarsOptimizedTemplate template =
                            templateEngine.getTemplate(
                                HttpTemplateCacheKey.forHeader(
                                    responseDefinition, header.key(), index++),
                                headerValue);
                        valueListBuilder.add(uncheckedApplyTemplate(template, model));
                      }
                      return new HttpHeader(header.key(), valueListBuilder);
                    })
                .collect(Collectors.toList());
        newProxyResponseDefBuilder.withAdditionalRequestHeaders(
            new HttpHeaders(newProxyResponseResponseHeaders));

        return newProxyResponseDefBuilder.build();
      } else {
        return newResponseDefBuilder.build();
      }
    } catch (HandlebarsException he) {
      final String message = cleanUpHandlebarsErrorMessage(he);
      serveEvent.appendSubEvent(SubEvent.error(message));
      return serverError()
          .withHeader(ContentTypeHeader.KEY, "text/plain")
          .withBody(message)
          .build();
    }
  }

  private static String cleanUpHandlebarsErrorMessage(HandlebarsException t) {
    String rawMessage;
    if (t.getCause() instanceof JsonException) {
      rawMessage = ((JsonException) t.getCause()).getErrors().first().getDetail();
    } else {
      rawMessage = t.getMessage() == null ? "" : t.getMessage();
    }

    var message =
        rawMessage
            .replaceAll("\\n\\s*inline@[a-z0-9]+:\\S+$", "")
            .replaceAll("inline@[a-z0-9]+:", "");
    return "[ERROR] " + message;
  }

  /** Override this to add extra elements to the template model */
  protected Map<String, Object> addExtraModelElements(
      Request request,
      ResponseDefinition responseDefinition,
      FileSource files,
      Parameters parameters) {
    return Collections.emptyMap();
  }

  private TextEntityDefinition applyTemplateToBodyEntity(
      Map<String, Object> model, HandlebarsOptimizedTemplate bodyTemplate) {
    String bodyString = uncheckedApplyTemplate(bodyTemplate, model);
    return (TextEntityDefinition)
        EntityDefinition.builder().setEncoding(TEXT).setFormat(JSON).setData(bodyString).build();
  }

  private String uncheckedApplyTemplate(HandlebarsOptimizedTemplate template, Object context) {
    return template.apply(context);
  }

  @Override
  public void afterStubRemoved(StubMapping stub) {
    templateEngine.invalidateCache();
  }

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
}
