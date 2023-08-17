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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.jknack.handlebars.HandlebarsException;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.extension.*;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ResponseTemplateTransformer
    implements StubLifecycleListener, ResponseDefinitionTransformerV2 {

  public static final String NAME = "response-template";

  private final boolean global;
  private final FileSource files;
  private final TemplateEngine templateEngine;

  private final List<TemplateModelDataProviderExtension> templateModelDataProviders;

  public ResponseTemplateTransformer(
      TemplateEngine templateEngine,
      boolean global,
      FileSource files,
      List<TemplateModelDataProviderExtension> templateModelDataProviders) {
    this.templateEngine = templateEngine;
    this.global = global;
    this.files = files;
    this.templateModelDataProviders = templateModelDataProviders;
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
      final Parameters parameters =
          getFirstNonNull(responseDefinition.getTransformerParameters(), Parameters.empty());

      ResponseDefinitionBuilder newResponseDefBuilder =
          ResponseDefinitionBuilder.like(responseDefinition);

      final PathTemplate pathTemplate =
          serveEvent.getStubMapping().getRequest().getUrlMatcher().getPathTemplate();

      final Map<String, Object> additionalModelData =
          templateModelDataProviders.stream()
              .map(provider -> provider.provideTemplateModelData(serveEvent).entrySet())
              .flatMap(Set::stream)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      final ImmutableMap<String, Object> model =
          ImmutableMap.<String, Object>builder()
              .put("parameters", parameters)
              .put("request", RequestTemplateModel.from(request, pathTemplate))
              .putAll(addExtraModelElements(request, responseDefinition, files, parameters))
              .putAll(additionalModelData)
              .build();

      if (responseDefinition.specifiesTextBodyContent()) {
        boolean isJsonBody = responseDefinition.getReponseBody().isJson();
        HandlebarsOptimizedTemplate bodyTemplate =
            templateEngine.getTemplate(
                HttpTemplateCacheKey.forInlineBody(responseDefinition),
                responseDefinition.getTextBody());
        applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate, isJsonBody);
      } else if (responseDefinition.specifiesBodyFile()) {
        HandlebarsOptimizedTemplate filePathTemplate =
            templateEngine.getUncachedTemplate(responseDefinition.getBodyFileName());
        String compiledFilePath = uncheckedApplyTemplate(filePathTemplate, model);

        boolean disableBodyFileTemplating =
            parameters.getBoolean("disableBodyFileTemplating", false);
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
          List<HttpHeader> newResponseHeaders =
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
    } catch (HandlebarsException he) {
      final String message = cleanUpHandlebarsErrorMessage(he.getMessage());
      serveEvent.appendSubEvent(SubEvent.error(message));
      return serverError()
          .withHeader(ContentTypeHeader.KEY, "text/plain")
          .withBody(message)
          .build();
    }
  }

  private static String cleanUpHandlebarsErrorMessage(String rawMessage) {
    return rawMessage.replaceAll("inline@[a-z0-9]+:", "").replaceAll("\n.*", "");
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
    String bodyString = uncheckedApplyTemplate(bodyTemplate, model);
    Body body =
        isJsonBody
            ? Body.fromJsonBytes(bodyString.getBytes(StandardCharsets.UTF_8))
            : Body.fromOneOf(null, bodyString, null, null);
    newResponseDefBuilder.withResponseBody(body);
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
