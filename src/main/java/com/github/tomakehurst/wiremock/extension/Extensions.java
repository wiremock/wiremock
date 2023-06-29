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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.store.Stores;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.LinkedHashMap;
import java.util.Map;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

public class Extensions {

  public static Extensions NONE = new Extensions(new ExtensionDeclarations(), null, null, null);

  private final ExtensionDeclarations extensionDeclarations;
  private final Admin admin;
  private final Stores stores;
  private final FileSource files;

  private final Map<String, Extension> loadedExtensions;

  @SuppressWarnings("unchecked")
  public Extensions(
      ExtensionDeclarations extensionDeclarations, Admin admin, Stores stores, FileSource files) {
    this.extensionDeclarations = extensionDeclarations;
    this.admin = admin;
    this.stores = stores;
    this.files = files;

    loadedExtensions = new LinkedHashMap<>();
  }

  public void load() {
    final ServiceLocatorFactory serviceLocatorFactory = ServiceLocatorFactory.getInstance();
    final ServiceLocator serviceLocator = serviceLocatorFactory.create("default");

    ServiceLocatorUtilities.addOneConstant(serviceLocator, admin, "Admin", Admin.class);
    ServiceLocatorUtilities.addOneConstant(serviceLocator, stores, "Stores", Stores.class);
    ServiceLocatorUtilities.addOneConstant(serviceLocator, files, "FileSource", FileSource.class);

    Streams.concat(
            extensionDeclarations.getClassNames().stream().map(Extensions::loadClass),
            extensionDeclarations.getClasses().stream())
        .map(serviceLocator::create)
        .forEach(
            extension -> {
              if (loadedExtensions.containsKey(extension.getName())) {
                throw new IllegalArgumentException(
                    "Duplicate extension name: " + extension.getName());
              }
              loadedExtensions.put(extension.getName(), extension);
            });

    loadedExtensions.putAll(extensionDeclarations.getInstances());

    serviceLocatorFactory.destroy("default");
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Extension> loadClass(String className) {
    try {
      return (Class<? extends Extension>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      return throwUnchecked(e, Class.class);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Extension> Map<String, T> ofType(Class<T> extensionType) {
    return (Map<String, T>)
        Maps.filterEntries(loadedExtensions, valueAssignableFrom(extensionType)::test);
  }
}
