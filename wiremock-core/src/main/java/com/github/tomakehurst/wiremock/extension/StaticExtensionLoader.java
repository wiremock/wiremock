/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.http.DefaultFactory;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class StaticExtensionLoader<T extends Extension> {

  private final Class<T> type;
  private T specificInstance = null;
  private Collection<T> extensions = List.of();
  private Supplier<Stream<T>> serviceLoader;

  public StaticExtensionLoader(Class<T> type) {
    this.type = type;
    this.serviceLoader = systemServiceLoader(type);
  }

  public T load() {
    if (specificInstance != null) {
      return specificInstance;
    }

    if (!extensions.isEmpty()) {
      return pickMostAppropriateFrom(extensions.stream());
    }

    return pickMostAppropriateFrom(serviceLoader.get());
  }

  public StaticExtensionLoader<T> setSpecificInstance(T specificInstance) {
    this.specificInstance = specificInstance;
    return this;
  }

  public StaticExtensionLoader<T> setExtensions(List<T> extensions) {
    this.extensions = extensions;
    return this;
  }

  public StaticExtensionLoader<T> setExtensions(Extensions extensions) {
    this.extensions = extensions.ofType(type).values();
    return this;
  }

  public StaticExtensionLoader<T> setServiceLoader(Supplier<Stream<T>> serviceLoader) {
    this.serviceLoader = serviceLoader;
    return this;
  }

  private T pickMostAppropriateFrom(Stream<T> candidates) {
    return candidates
        .min(defaultFactoryLast())
        .orElseThrow(this::couldNotFindSuitableImplementationException);
  }

  private Comparator<Object> defaultFactoryLast() {
    return (o1, o2) -> {
      boolean o1IsDefault = o1 instanceof DefaultFactory;
      boolean o2IsDefault = o2 instanceof DefaultFactory;
      if (o1IsDefault == o2IsDefault) {
        return 0;
      } else if (o1IsDefault) {
        return 1;
      } else {
        return -1;
      }
    };
  }

  private FatalStartupException couldNotFindSuitableImplementationException() {
    return new FatalStartupException(
        "No suitable "
            + type.getSimpleName()
            + " was found. Please ensure that the classpath includes a WireMock extension that provides an "
            + type.getSimpleName()
            + " implementation. See https://wiremock.org/docs/extending-wiremock/ for more information.");
  }

  private static <T extends Extension> Supplier<Stream<T>> systemServiceLoader(Class<T> type) {
    //noinspection unchecked
    return () ->
        ServiceLoader.load(Extension.class).stream()
            .filter(extension -> type.isAssignableFrom(extension.type()))
            .map(e -> (T) e.get());
  }
}
