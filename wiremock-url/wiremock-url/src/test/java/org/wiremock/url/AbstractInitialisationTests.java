/*
 * Copyright (C) 2026 Thomas Akehurst
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
package org.wiremock.url;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

abstract class AbstractInitialisationTests {

  final String className;
  final String parserName;
  final String inputToTest;

  static final String EMPTY = "EMPTY";
  static final String INSTANCE = "INSTANCE";
  static final String parse = "parse";
  final String staticField;

  AbstractInitialisationTests(
      String className, String staticField, String parserName, String inputToTest) {
    this.className = className;
    this.parserName = parserName;
    this.inputToTest = inputToTest;
    this.staticField = staticField;
  }

  @Test
  void staticFieldIsInitialised() throws Exception {
    try (IsolatedClassLoader classLoader = new IsolatedClassLoader()) {
      assertStaticFieldInitialised(classLoader);
    }
  }

  @Test
  void parseInputWorks() throws Exception {
    try (IsolatedClassLoader classLoader = new IsolatedClassLoader()) {
      var parsed = classLoader.load(className).invoke(parse, inputToTest);
      assertThat(parsed).hasToString(inputToTest);

      assertStaticFieldInitialised(classLoader);
    }
  }

  @Test
  void parserInstanceParse() throws Throwable {
    try (IsolatedClassLoader classLoader = new IsolatedClassLoader()) {
      var parser = getParserInstance(classLoader);
      var parsed = parser.invoke(parse, inputToTest);
      assertThat(parsed).hasToString(inputToTest);

      assertStaticFieldInitialised(classLoader);
    }
  }

  ReflectiveInstance getParserInstance(IsolatedClassLoader classLoader)
      throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
    return requireNonNull(classLoader.load(parserName).field(INSTANCE));
  }

  void assertStaticFieldInitialised(IsolatedClassLoader classLoader)
      throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
    var staticField = classLoader.load(className).field(this.staticField);
    assertThat(staticField).hasToString(inputToTest);
  }
}
