/*
 * Copyright (C) 2014-2026 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Exceptions;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExtensionLoader {

  private ExtensionLoader() {}

  @SuppressWarnings("unchecked")
  public static <T> Map<String, T> loadExtension(String... classNames) {
    return (Map<String, T>)
        asMap(
            Arrays.stream(classNames)
                .map(ExtensionLoader::toClasses)
                .map(ExtensionLoader::toExtension)
                .collect(Collectors.toList()));
  }

  private static Class<? extends Extension> toClasses(String className) {
    return Exceptions.uncheck(
        (Callable<Class<? extends Extension>>)
            () -> Class.forName(className).asSubclass(Extension.class));
  }

  public static Map<String, Extension> load(String... classNames) {
    return loadExtension(classNames);
  }

  public static Map<String, Extension> asMap(List<Extension> extensions) {
    return extensions.stream()
        .map(extension -> Map.entry(extension.getName(), extension))
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @SafeVarargs
  public static Map<String, Extension> load(Class<? extends Extension>... classes) {
    return asMap(
        Arrays.stream(classes).map(ExtensionLoader::toExtension).collect(Collectors.toList()));
  }

  private static Extension toExtension(Class<? extends Extension> extensionClass) {
    try {
      return extensionClass.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return throwUnchecked(e, Extension.class);
    }
  }

  public static <T extends Extension> Predicate<Map.Entry<String, Extension>> valueAssignableFrom(
      final Class<T> extensionType) {
    return input -> extensionType.isAssignableFrom(input.getValue().getClass());
  }
}
