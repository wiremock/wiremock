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

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ClientExtensions extends Extensions {

  public ClientExtensions(ExtensionDeclarations extensionDeclarations) {
    super(extensionDeclarations);
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
    final Stream<ExtensionFactory> allFactories = extensionDeclarations.getFactories().stream();

    loadedExtensions.putAll(
        allFactories
            .map(ExtensionFactory::createForClient)
            .flatMap(List::stream)
            .collect(toMap(Extension::getName, Function.identity())));
  }
}
