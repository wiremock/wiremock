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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

abstract class AbstractEncodableInitialisationTests extends AbstractInitialisationTests {

  static final String encode = "encode";

  AbstractEncodableInitialisationTests(
      String className, String staticField, String parserName, String inputToTest) {
    super(className, staticField, parserName, inputToTest);
  }

  @Test
  void encodeInputWorks() throws Exception {
    try (IsolatedClassLoader classLoader = new IsolatedClassLoader()) {
      var encoded = classLoader.load(className).invoke(encode, inputToTest);
      assertThat(encoded).hasToString(inputToTest);

      assertStaticFieldInitialised(classLoader);
    }
  }

  @Test
  void parserInstanceEncode() throws Throwable {
    try (IsolatedClassLoader classLoader = new IsolatedClassLoader()) {
      var parser = getParserInstance(classLoader);
      var encoded = parser.invoke(encode, inputToTest);
      assertThat(encoded).hasToString(inputToTest);

      assertStaticFieldInitialised(classLoader);
    }
  }
}
