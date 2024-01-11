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

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static java.util.Arrays.asList;

import java.util.*;
import java.util.stream.Collectors;
import org.wiremock.webhooks.Webhooks;

public class ExtensionDeclarations {

  private final List<String> classNames;
  private final List<Class<? extends Extension>> classes;
  private final Map<String, Extension> instances;
  private final List<ExtensionFactory> factories;
  private static final String WEBHOOK_MESSAGE =
      "Passing webhooks in extensions is no longer required and"
          + " may lead to compatibility issues in future";

  public ExtensionDeclarations() {
    this.classNames = new ArrayList<>();
    this.classes = new ArrayList<>();
    this.instances = new LinkedHashMap<>();
    this.factories = new ArrayList<>();
  }

  public void add(String... classNames) {
    List<String> processedClassNames =
        Arrays.stream(classNames).filter(this::removeWebhook).collect(Collectors.toList());
    this.classNames.addAll(processedClassNames);
  }

  public void add(Extension... extensionInstances) {
    Arrays.stream(extensionInstances).forEach(e -> instances.put(e.getName(), e));
  }

  public void add(Class<? extends Extension>... classes) {
    List<Class<? extends Extension>> processedClasses =
        Arrays.stream(classes).filter(c -> removeWebhook(c.getName())).collect(Collectors.toList());
    this.classes.addAll(processedClasses);
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

  private boolean removeWebhook(String className) {
    if (className.equals(Webhooks.class.getName())) {
      notifier().info(WEBHOOK_MESSAGE);
      return false;
    }
    return true;
  }
}
