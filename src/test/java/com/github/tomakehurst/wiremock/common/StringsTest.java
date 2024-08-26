/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringsTest {

  static Stream<Arguments> wrapIfLongestLineExceedsLimitSource() {
    return Stream.of(
        arguments("abc", "a\nb\nc", 1),
        arguments("abc", "ab\nc", 2),
        arguments("abc", "abc", 3),
        arguments("abc\nabcd\nabcd", "abc\nab\ncd\nabc\nd", 3));
  }

  @ParameterizedTest
  @MethodSource("wrapIfLongestLineExceedsLimitSource")
  void shouldWrapIfLongestLineExceedsLimit(String input, String expected, int limit) {
    // when
    String actual = Strings.wrapIfLongestLineExceedsLimit(input, limit);

    // then
    assertEquals(expected, actual);
  }
}
