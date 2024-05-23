/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.extension.ExtensionLoader.valueAssignableFrom;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.JsonException;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.LazyTemplateEngine;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.LazyHttpClient;
import com.github.tomakehurst.wiremock.http.client.LazyHttpClientFactory;
import com.github.tomakehurst.wiremock.matching.ContentPatternExtension;
import com.github.tomakehurst.wiremock.store.Stores;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wiremock.webhooks.WebhookTransformer;
import org.wiremock.webhooks.Webhooks;

public class Extensions implements WireMockServices {

  public static final Extensions NONE =
      new Extensions(new ExtensionDeclarations(), null, null, null, null);

  private final ExtensionDeclarations extensionDeclarations;
  private final Admin admin;

  private final Options options;
  private final Stores stores;
  private final FileSource files;

  private TemplateEngine templateEngine;

  private HttpClientFactory httpClientFactory;

  private final Map<String, Extension> loadedExtensions;

  public Extensions(
      ExtensionDeclarations extensionDeclarations,
      Admin admin,
      Options options,
      Stores stores,
      FileSource files) {
    this.extensionDeclarations = extensionDeclarations;
    this.admin = admin;
    this.options = options;
    this.stores = stores;
    this.files = files;

    loadedExtensions = new LinkedHashMap<>();
  }

  public void load() {
    Stream.concat(
            extensionDeclarations.getClassNames().stream().map(Extensions::loadClass),
            extensionDeclarations.getClasses().stream())
        .map(Extensions::load)
        .forEach(
            extension -> {
              if (loadedExtensions.containsKey(extension.getName())) {
                throw new IllegalArgumentException(
                    "Duplicate extension name: " + extension.getName());
              }
              loadedExtensions.put(extension.getName(), extension);
            });

    loadedExtensions.putAll(extensionDeclarations.getInstances());

    if (options.isExtensionScanningEnabled()) {
      loadedExtensions.putAll(
          loadExtensionsAsServices().collect(toMap(Extension::getName, Function.identity())));
    }

    final Stream<ExtensionFactory> allFactories =
        options.isExtensionScanningEnabled()
            ? Stream.concat(
                extensionDeclarations.getFactories().stream(), loadExtensionFactoriesAsServices())
            : extensionDeclarations.getFactories().stream();

    loadedExtensions.putAll(
        allFactories
            .map(factory -> factory.create(Extensions.this))
            .flatMap(List::stream)
            .collect(toMap(Extension::getName, Function.identity())));

    configureTemplating();
    configureHttpClient();
    configureWebhooks();
  }

  private Stream<Extension> loadExtensionsAsServices() {
    final ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class);
    return loader.stream().map(ServiceLoader.Provider::get);
  }

  private Stream<ExtensionFactory> loadExtensionFactoriesAsServices() {
    final ServiceLoader<ExtensionFactory> loader = ServiceLoader.load(ExtensionFactory.class);
    return loader.stream().map(ServiceLoader.Provider::get);
  }

  private void configureTemplating() {
    final Map<String, Helper<?>> helpers =
        ofType(TemplateHelperProviderExtension.class).values().stream()
            .map(TemplateHelperProviderExtension::provideTemplateHelpers)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    final List<TemplateModelDataProviderExtension> templateModelProviders =
        new ArrayList<>(ofType(TemplateModelDataProviderExtension.class).values());

    templateEngine =
        new TemplateEngine(
            helpers,
            options.getMaxTemplateCacheEntries(),
            options.getTemplatePermittedSystemKeys(),
            options.getTemplateEscapingDisabled(),
            templateModelProviders);

    if (options.getResponseTemplatingEnabled()) {
      final ResponseTemplateTransformer responseTemplateTransformer =
          new ResponseTemplateTransformer(
              getTemplateEngine(),
              options.getResponseTemplatingGlobal(),
              getFiles(),
              templateModelProviders);
      loadedExtensions.put(responseTemplateTransformer.getName(), responseTemplateTransformer);
    }
  }

  private void configureHttpClient() {
    httpClientFactory =
        ofType(com.github.tomakehurst.wiremock.http.client.HttpClientFactory.class)
            .values()
            .stream()
            .findFirst()
            .orElse(options.httpClientFactory());
  }

  private void configureWebhooks() {
    final List<WebhookTransformer> webhookTransformers =
        ofType(WebhookTransformer.class).values().stream().collect(Collectors.toUnmodifiableList());

    final Webhooks webhooks =
        new Webhooks(this, Executors.newScheduledThreadPool(10), webhookTransformers);
    loadedExtensions.put(webhooks.getName(), webhooks);
  }

  @Override
  public Admin getAdmin() {
    return admin;
  }

  @Override
  public Stores getStores() {
    return stores;
  }

  @Override
  public FileSource getFiles() {
    return files;
  }

  @Override
  public Options getOptions() {
    return options;
  }

  @Override
  public Extensions getExtensions() {
    return this;
  }

  @Override
  public TemplateEngine getTemplateEngine() {
    return new LazyTemplateEngine(() -> templateEngine);
  }

  @Override
  public HttpClientFactory getHttpClientFactory() {
    return new LazyHttpClientFactory(() -> httpClientFactory);
  }

  @Override
  public HttpClient getDefaultHttpClient() {
    return new LazyHttpClient(
        () -> httpClientFactory.buildHttpClient(options, true, Collections.emptyList(), true));
  }

  public int getCount() {
    return loadedExtensions.size();
  }

  public Set<String> getAllExtensionNames() {
    return loadedExtensions.keySet();
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Extension> loadClass(String className) {
    try {
      return (Class<? extends Extension>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      return throwUnchecked(e, Class.class);
    }
  }

  public static Extension load(Class<? extends Extension> extensionClass) {
    try {
      return extensionClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return throwUnchecked(e, Extension.class);
    }
  }

  public void startAll() {
    loadedExtensions.values().forEach(Extension::start);
  }

  public void stopAll() {
    loadedExtensions.values().forEach(Extension::stop);
  }

  @SuppressWarnings("unchecked")
  public <T extends Extension> Map<String, T> ofType(Class<T> extensionType) {
    return (Map<String, T>)
        Collections.unmodifiableMap(
            loadedExtensions.entrySet().stream()
                .filter(valueAssignableFrom(extensionType))
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (entry1, entry2) -> entry1,
                        LinkedHashMap::new)));
  }

  public <T> T read(String content, Class<T> valueType) {
    try {
      List<Class<?>> contentPatternExtensions =
          ofType(ContentPatternExtension.class).values().stream()
              .map(ContentPatternExtension::getContentPatternClass)
              .collect(Collectors.toList());
      ObjectMapper objectMapper = Json.getObjectMapper().copy();
      objectMapper.registerSubtypes(contentPatternExtensions);
      return objectMapper.readValue(content, valueType);
    } catch (JsonProcessingException processingException) {
      throw JsonException.fromJackson(processingException);
    }
  }
}
