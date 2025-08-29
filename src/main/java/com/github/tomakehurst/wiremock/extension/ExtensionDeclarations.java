/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.wiremock.webhooks.Webhooks;

/** The type Extension declarations. */
public class ExtensionDeclarations {

  private final List<String> classNames;
  private final List<Class<? extends Extension>> classes;
  private final Map<String, Extension> instances;
  private final List<Class<? extends ExtensionFactory>> factoryClasses;
  private final List<ExtensionFactory> factories;
  private static final String WEBHOOK_MESSAGE =
      "Passing webhooks in extensions is no longer required and"
          + " may lead to compatibility issues in future";

  /** Instantiates a new Extension declarations. */
  public ExtensionDeclarations() {
    this.classNames = new ArrayList<>();
    this.classes = new ArrayList<>();
    this.instances = new LinkedHashMap<>();
    this.factoryClasses = new ArrayList<>();
    this.factories = new ArrayList<>();
  }

  /**
   * Add.
   *
   * @param classNames the class names
   */
  public void add(String... classNames) {
    List<String> processedClassNames =
        Arrays.stream(classNames).filter(this::removeWebhook).collect(Collectors.toList());
    this.classNames.addAll(processedClassNames);
  }

  /**
   * Add.
   *
   * @param extensionInstances the extension instances
   */
  public void add(Extension... extensionInstances) {
    Arrays.stream(extensionInstances).forEach(e -> instances.put(e.getName(), e));
  }

  /**
   * Add.
   *
   * @param classes the classes
   */
  public void add(Class<? extends Extension>... classes) {
    List<Class<? extends Extension>> processedClasses =
        Arrays.stream(classes).filter(c -> removeWebhook(c.getName())).collect(Collectors.toList());
    this.classes.addAll(processedClasses);
  }

  /**
   * Add.
   *
   * @param factories the factories
   */
  public void add(ExtensionFactory... factories) {
    this.factories.addAll(asList(factories));
  }

  /**
   * Add factories.
   *
   * @param factoryClasses the factory classes
   */
  public void addFactories(Class<? extends ExtensionFactory>... factoryClasses) {
    this.factoryClasses.addAll(asList(factoryClasses));
  }

  /**
   * Gets class names.
   *
   * @return the class names
   */
  public List<String> getClassNames() {
    return classNames;
  }

  /**
   * Gets classes.
   *
   * @return the classes
   */
  public List<Class<? extends Extension>> getClasses() {
    return classes;
  }

  /**
   * Gets instances.
   *
   * @return the instances
   */
  public Map<String, Extension> getInstances() {
    return instances;
  }

  /**
   * Gets factories.
   *
   * @return the factories
   */
  public List<ExtensionFactory> getFactories() {
    return factories;
  }

  /**
   * Gets factory classes.
   *
   * @return the factory classes
   */
  public List<Class<? extends ExtensionFactory>> getFactoryClasses() {
    return factoryClasses;
  }

  private boolean removeWebhook(String className) {
    if (className.equals(Webhooks.class.getName())) {
      System.out.println(WEBHOOK_MESSAGE);
      return false;
    }
    return true;
  }
}
