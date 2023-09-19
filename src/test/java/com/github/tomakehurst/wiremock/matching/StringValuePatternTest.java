/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
class StringValuePatternTest {

  @Test
  void allSubclassesHaveWorkingToString() throws Exception {
    Set<Class> matchingClasses = getClassOfPackage("com.github.tomakehurst.wiremock.matching");

    var matchingClassesAfterFilter01 =
        matchingClasses.stream()
            .filter(clazz -> clazz.isAssignableFrom(StringValuePattern.class))
            .collect(Collectors.toSet());

    var matchingClassesAfterFilter02 =
        matchingClassesAfterFilter01.stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .collect(Collectors.toSet());

    assertDoesNotThrow(
        () ->
            matchingClassesAfterFilter02.forEach(
                this::findConstructorWithStringParamInFirstPosition));
  }

  public Set<Class> getClassOfPackage(String packageName)
      throws ClassNotFoundException, IOException {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    assert classLoader != null;
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);
    List<File> dirs = new ArrayList<>();
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    Set<Class> classes = new HashSet<>();
    for (File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes;
  }

  private Set<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
    Set<Class> classes = new HashSet<>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : Objects.requireNonNull(files)) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(
            Class.forName(
                packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }

  private void findConstructorWithStringParamInFirstPosition(Class<?> clazz) {
    Arrays.stream(clazz.getConstructors())
        .filter(
            constructor ->
                constructor.getParameterTypes().length > 0
                    && constructor.getParameterTypes()[0].equals(String.class)
                    && constructor.getParameterAnnotations().length > 0
                    && constructor.getParameterAnnotations()[0].length > 0
                    && constructor
                        .getParameterAnnotations()[0][0]
                        .annotationType()
                        .equals(JsonProperty.class))
        .findFirst()
        .orElseThrow(
            () ->
                new AssertionError(
                    "No constructor found with @JsonProperty annotated name parameter"));
  }
}
