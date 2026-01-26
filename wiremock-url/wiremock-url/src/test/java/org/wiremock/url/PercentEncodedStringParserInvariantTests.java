/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

public class PercentEncodedStringParserInvariantTests {

  static <T extends PercentEncoded<T>> Stream<DynamicTest> generateEncodeDecodeInvariantTests(
      PercentEncodedStringParser<T> parser, Stream<String> decodedForms) {
    return decodedForms.flatMap(
        original ->
            Stream.of(
                dynamicTest(
                    "result of encoding `" + original + "` is in normal form",
                    () -> {
                      T encoded = parser.encode(original);
                      assertThat(encoded.isNormalForm());

                      T parsed = parser.parse(encoded.toString());
                      assertThat(parsed).isEqualTo(encoded);
                      assertThat(parsed.isNormalForm());
                    }),
                dynamicTest(
                    "Original -> Encode -> Decode `" + original + "` produces the original value",
                    () -> {
                      T encoded = parser.encode(original);
                      String decoded = encoded.decode();
                      assertThat(decoded).isEqualTo(original);
                    })));
  }
}
