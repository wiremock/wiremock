/*
 * Copyright (C) 2025 Thomas Akehurst
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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

/**
 * Reusable test utility for verifying CharSequenceParser invariants.
 *
 * <p>This class provides methods to generate dynamic tests that verify any CharSequenceParser
 * implementation maintains the following invariants:
 *
 * <ul>
 *   <li>The {@code toString()} of the parsed instance equals the original input string
 *   <li>Values parsed from non-equal strings are never equal to each other
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * class MyParserTests {
 *   @TestFactory
 *   Stream<DynamicTest> parser_invariants() {
 *     List<String> validInputs = List.of("value1", "value2", "value3");
 *     return CharSequenceParserInvariantTests.generateInvariantTests(
 *         MyParser.INSTANCE, validInputs);
 *   }
 * }
 * }</pre>
 */
public class CharSequenceParserInvariantTests {

  /**
   * Generates dynamic tests that verify all CharSequenceParser invariants for the provided valid
   * input strings.
   *
   * <p>This method returns a Stream of DynamicTest that can be used with JUnit's
   * {@code @TestFactory} annotation.
   *
   * @param parser the CharSequenceParser to test
   * @param validStrings a collection of valid string values that the parser should successfully
   *     parse
   * @param <T> the type returned by the parser
   * @return a Stream of DynamicTest instances
   */
  public static <T> Stream<DynamicTest> generateInvariantTests(
      CharSequenceParser<T> parser, Collection<String> validStrings) {
    List<DynamicTest> tests = new ArrayList<>();

    // Generate toString preservation tests
    for (String input : validStrings) {
      tests.add(
          dynamicTest(
              "toString() preserves input: '" + input + "'",
              () -> {
                T parsed = parser.parse(input);
                assertThat(parsed.toString()).isEqualTo(input);
              }));
    }

    // Generate non-equal strings tests
    String[] strings = validStrings.toArray(new String[0]);
    for (int i = 0; i < strings.length; i++) {
      for (int j = i + 1; j < strings.length; j++) {
        String string1 = strings[i];
        String string2 = strings[j];

        if (!string1.equals(string2)) {
          tests.add(
              dynamicTest(
                  "Different strings produce non-equal values: '"
                      + string1
                      + "' vs '"
                      + string2
                      + "'",
                  () -> {
                    T parsed1 = parser.parse(string1);
                    T parsed2 = parser.parse(string2);
                    assertThat(parsed1).isNotEqualTo(parsed2);
                  }));
        }
      }
    }

    return tests.stream();
  }
}
