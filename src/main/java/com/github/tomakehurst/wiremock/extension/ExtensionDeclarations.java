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

import static java.util.Arrays.asList;

import java.util.*;

public class ExtensionDeclarations {

  private final List<String> classNames;
  private final List<Class<? extends Extension>> classes;
  private final Map<String, Extension> instances;
  private final List<ExtensionFactory> factories;

  public ExtensionDeclarations() {
    this.classNames = new ArrayList<>();
    this.classes = new ArrayList<>();
    this.instances = new LinkedHashMap<>();
    this.factories = new ArrayList<>();
  }

  public void add(String... classNames) {
    this.classNames.addAll(asList(classNames));
  }

  public void add(Extension... extensionInstances) {
    Arrays.stream(extensionInstances).forEach(e -> instances.put(e.getName(), e));
  }

  public void add(Class<? extends Extension>... classes) {
    this.classes.addAll(asList(classes));
  }

  public void add(ExtensionFactory... factories) {
    this.factories.addAll(asList(factories));
  }

  public List<String> getClassNames() {
    return classNames;
  }

  public List<Class<? extends Extension>> getClasses() {
    return classes;
  }

  public Map<String, Extension> getInstances() {
    return instances;
  }

  public List<ExtensionFactory> getFactories() {
    return factories;
  }
}
