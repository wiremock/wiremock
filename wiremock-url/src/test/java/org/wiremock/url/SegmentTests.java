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

import static org.wiremock.url.PercentEncodedStringParserInvariantTests.generateEncodeDecodeInvariantTests;
import static org.wiremock.url.PercentEncodedStringParserInvariantTests.generateNormaliseDecodeEncodeInvariantTests;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

class SegmentTests {

  @Nested
  class Parse {

    static final List<String> validSegments =
        List.of(
            "",
            "segment",
            "%3F",
            "seg%3Fment",
            "seg%3fment",
            "seg  ment",
            "{}",
            "%3Fsegment",
            "segment%3F",
            "%23",
            "seg%23ment",
            "%23segment",
            "segment%23",
            "%2F",
            "seg%2Fment",
            "%2Fsegment",
            "segment%2F");

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          SegmentParser.INSTANCE, validSegments);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "?",
          "seg?ment",
          "?segment",
          "segment?",
          "#",
          "seg#ment",
          "#segment",
          "segment#",
          "/",
          "seg/ment",
          "/segment",
          "segment/",
        })
    void rejects_illegal_segments(String illegalSegment) {
      Assertions.assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> Segment.parse(illegalSegment))
          .withMessage("Illegal segment: `" + illegalSegment + "`")
          .withNoCause()
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalSegment);
    }
  }

  @Nested
  class Codec {

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      var decoded =
          Stream.of(
              "?",
              "seg?ment",
              "?segment",
              "segment?",
              "#",
              "seg#ment",
              "#segment",
              "segment#",
              "/",
              "seg/ment",
              "/segment",
              "segment/");

      return generateEncodeDecodeInvariantTests(FragmentParser.INSTANCE, decoded);
    }

    @TestFactory
    Stream<DynamicTest> normalise_decode_encode_invariants() {

      var encoded =
          Stream.of(
              "",
              "segment",
              "%3F",
              "seg%3Fment",
              "%3Fsegment",
              "segment%3F",
              "%23",
              "seg%23ment",
              "%23segment",
              "segment%23",
              "%2F",
              "seg%2Fment",
              "%2Fsegment",
              "segment%2F");

      return generateNormaliseDecodeEncodeInvariantTests(FragmentParser.INSTANCE, encoded);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<Segment>> normalisationCases =
        Stream.of(
                // Characters that need encoding
                Pair.of("section name", "section%20name"),
                Pair.of("hello world", "hello%20world"),
                Pair.of("test\"quote", "test%22quote"),
                Pair.of("test<tag>", "test%3Ctag%3E"),
                Pair.of("test`backtick", "test%60backtick"),
                Pair.of("data{value}", "data%7Bvalue%7D"),
                Pair.of("test{name}", "test%7Bname%7D"),
                Pair.of("café", "caf%C3%A9"),
                Pair.of("héllo", "h%C3%A9llo"),
                Pair.of("%ff", "%FF"),
                Pair.of("%fF", "%FF"),
                Pair.of("%Ff", "%FF"),
                Pair.of("%41", "A"),
                Pair.of("%5A", "Z"),
                Pair.of("%5a", "Z"))
            .map(
                testCase ->
                    new NormalisationCase<>(
                        Segment.parse(testCase.getLeft()), Segment.parse(testCase.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_segment_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases);
    }

    static final List<Segment> alreadyNormalised =
        Stream.of(
                "",
                "section",
                "section-name",
                "section_name.test~123",
                "section!name",
                "time:12:30",
                "section%20name",
                "caf%C3%A9",
                "test%22quote")
            .map(Segment::parse)
            .toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(alreadyNormalised);
    }
  }
}
