/*
 * Copyright (C) 2024 Thomas Akehurst
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Extensions {

  protected final ExtensionDeclarations extensionDeclarations;
  protected final Map<String, Extension> loadedExtensions;

  public Extensions(ExtensionDeclarations extensionDeclarations) {
    this.extensionDeclarations = extensionDeclarations;
    this.loadedExtensions = new LinkedHashMap<>();
  }

  @SuppressWarnings("unchecked")
  protected static Class<? extends Extension> loadClass(String className) {
    try {
      return (Class<? extends Extension>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      return throwUnchecked(e, Class.class);
    }
  }

  public abstract void load();

  protected Stream<Extension> loadExtensionsAsServices() {
    final ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class);
    return loader.stream().map(ServiceLoader.Provider::get);
  }

  protected Stream<ExtensionFactory> loadExtensionFactoriesAsServices() {
    final ServiceLoader<ExtensionFactory> loader = ServiceLoader.load(ExtensionFactory.class);
    return loader.stream().map(ServiceLoader.Provider::get);
  }

  public int getCount() {
    return loadedExtensions.size();
  }

  public Set<String> getAllExtensionNames() {
    return loadedExtensions.keySet();
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
