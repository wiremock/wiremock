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
import com.google.common.reflect.ClassPath;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class StringValuePatternTest {

  @Test
  void allSubclassesHaveWorkingToString() throws Exception {
    Set<ClassPath.ClassInfo> allClasses =
        ClassPath.from(Thread.currentThread().getContextClassLoader()).getAllClasses();

    var allClassesAfterFilter01 =
        allClasses.stream()
            .filter(
                classInfo ->
                    classInfo
                        .getPackageName()
                        .startsWith("com.github.tomakehurst.wiremock.matching"))
            .collect(Collectors.toSet());

    var allClassesAfterMap =
        allClassesAfterFilter01.stream()
            .map(
                input -> {
                  try {
                    return input.load();
                  } catch (Throwable e) {
                    return Object.class;
                  }
                })
            .collect(Collectors.toSet());

    var allClassesAfterFilter02 =
        allClassesAfterMap.stream()
            .filter(clazz -> clazz.isAssignableFrom(StringValuePattern.class))
            .collect(Collectors.toSet());

    var allClassesAfterFilter03 =
        allClassesAfterFilter02.stream()
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
            .collect(Collectors.toSet());

    assertDoesNotThrow(
        () -> allClassesAfterFilter03.forEach(this::findConstructorWithStringParamInFirstPosition));
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
