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

import com.google.common.collect.Maps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.extension.ExtensionLoader.valueAssignableFrom;

public class Extensions {

  private final Map<String, Extension> loadedExtensions;

  @SuppressWarnings("unchecked")
  public Extensions(ExtensionDeclarations extensionDeclarations) {
    final Map<String, Extension> byClassName =
        ExtensionLoader.load(extensionDeclarations.getClassNames().toArray(String[]::new));
    final Map<String, Extension> byClass =
        ExtensionLoader.load(extensionDeclarations.getClasses().toArray(Class[]::new));
    loadedExtensions = new LinkedHashMap<>();
    Stream.of(byClassName, byClass, extensionDeclarations.getInstances())
        .map(Map::entrySet)
        .flatMap(Set::stream)
        .forEach(entry -> loadedExtensions.put(entry.getKey(), entry.getValue()));
  }

  @SuppressWarnings("unchecked")
  public <T extends Extension> Map<String, T> ofType(Class<T> extensionType) {
    return (Map<String, T>)
        Maps.filterEntries(loadedExtensions, valueAssignableFrom(extensionType)::test);
  }
}
