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
import static java.util.stream.Collectors.toMap;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.LazyTemplateEngine;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.LazyHttpClient;
import com.github.tomakehurst.wiremock.http.client.LazyHttpClientFactory;
import com.github.tomakehurst.wiremock.store.Stores;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wiremock.webhooks.WebhookTransformer;
import org.wiremock.webhooks.Webhooks;

public class ServerExtensions extends Extensions implements WireMockServices {

  public static final ServerExtensions NONE =
      new ServerExtensions(new ExtensionDeclarations(), null, null, null, null);

  private final Admin admin;

  private final Options options;
  private final Stores stores;
  private final FileSource files;

  private TemplateEngine templateEngine;

  private HttpClientFactory httpClientFactory;

  public ServerExtensions(
      ExtensionDeclarations extensionDeclarations,
      Admin admin,
      Options options,
      Stores stores,
      FileSource files) {
    super(extensionDeclarations);
    this.admin = admin;
    this.options = options;
    this.stores = stores;
    this.files = files;
  }

  public void load() {
    Stream.concat(
            extensionDeclarations.getClassNames().stream().map(Extensions::loadClass),
            extensionDeclarations.getClasses().stream())
        .map(ServerExtensions::load)
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
            .map(factory -> factory.create(ServerExtensions.this))
            .flatMap(List::stream)
            .collect(toMap(Extension::getName, Function.identity())));

    configureTemplating();
    configureHttpClient();
    configureWebhooks();
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
  public ServerExtensions getExtensions() {
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
}
