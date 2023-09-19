/*
 * Copyright (C) 2023 Thomas Akehurst
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

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.store.Stores;
import java.util.*;
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

    loadedExtensions.putAll(
        loadExtensionsAsServices().collect(toMap(Extension::getName, Function.identity())));

    final Stream<ExtensionFactory> allFactories =
        Stream.concat(
            extensionDeclarations.getFactories().stream(), loadExtensionFactoriesAsServices());
    loadedExtensions.putAll(
        allFactories
            .map(factory -> factory.create(Extensions.this))
            .flatMap(List::stream)
            .collect(toMap(Extension::getName, Function.identity())));

    configureTemplating();
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

    templateEngine =
        new TemplateEngine(
            helpers,
            options.getMaxTemplateCacheEntries(),
            options.getTemplatePermittedSystemKeys(),
            options.getTemplateEscapingDisabled());

    final List<TemplateModelDataProviderExtension> templateModelProviders =
        new ArrayList<>(ofType(TemplateModelDataProviderExtension.class).values());

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

  private void configureWebhooks() {
    final List<WebhookTransformer> webhookTransformers =
        ofType(WebhookTransformer.class).values().stream().collect(Collectors.toUnmodifiableList());

    final Webhooks webhooks = new Webhooks(webhookTransformers, options.getProxyTargetRules());
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
    return templateEngine;
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
}
