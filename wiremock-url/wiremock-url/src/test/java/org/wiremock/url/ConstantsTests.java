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
import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.includeRange;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class ConstantsTests {

  private static final boolean[] alphanumericDoNotNeedEncoding =
      combine(includeRange('a', 'z'), includeRange('A', 'Z'), includeRange('0', '9'));

  private static final List<Pair<String, String>> normalisationCases =
      List.of(
          Pair.of("%", "%25"),
          Pair.of("%0", "%250"),
          Pair.of("%F", "%25F"),
          Pair.of("%0G", "%250G"),
          Pair.of("%G0", "%25G0"),
          Pair.of("%/0", "%25%2F0"),
          Pair.of("%0/", "%250%2F"),
          Pair.of("%:0", "%25%3A0"),
          Pair.of("%0:", "%250%3A"),
          Pair.of("%@0", "%25%400"),
          Pair.of("%0@", "%250%40"),
          Pair.of("%[0", "%25%5B0"),
          Pair.of("%0[", "%250%5B"),
          Pair.of("%`0", "%25%600"),
          Pair.of("%0`", "%250%60"),
          Pair.of("%{0", "%25%7B0"),
          Pair.of("%0}", "%250%7D"),
          Pair.of("%00", "%00"),
          Pair.of("%09", "%09"),
          Pair.of("%99", "%99"),
          Pair.of("%a0", "%A0"),
          Pair.of("%A0", "%A0"),
          Pair.of("%a9", "%A9"),
          Pair.of("%A9", "%A9"),
          Pair.of("%aA", "%AA"),
          Pair.of("%Aa", "%AA"),
          Pair.of("%AA", "%AA"),
          Pair.of("%Af", "%AF"),
          Pair.of("%aF", "%AF"),
          Pair.of("%AF", "%AF"),
          Pair.of("%f0", "%F0"),
          Pair.of("%F0", "%F0"),
          Pair.of("%F9", "%F9"),
          Pair.of("%FA", "%FA"),
          Pair.of("%FF", "%FF"),
          Pair.of("%30", "0"),
          Pair.of("%39", "9"),
          Pair.of("%41", "A"),
          Pair.of("%5A", "Z"),
          Pair.of("%61", "a"),
          Pair.of("%7A", "z"),
          Pair.of("aßc", "a%C3%9Fc"),
          Pair.of("loC𝐀𝐋𝐇𝐨𝐬𝐭/usr/bin", "loC%F0%9D%90%80%F0%9D%90%8B%F0%9D%90%87%F0%9D%90%A8%F0%9D%90%AC%F0%9D%90%AD%2Fusr%2Fbin"),
          Pair.of("%~", "%25%7E"), // should be encoded
          Pair.of("%0~", "%250%7E"), // should be encoded
          Pair.of("%~0", "%25%7E0"), // should be encoded
          Pair.of("~", "%7E") // should be encoded
          );

  private static final List<Pair<String, String>> nonNormalNormalisationCases =
      normalisationCases.stream().filter(t -> !t.getLeft().equals(t.getRight())).toList();

  @ParameterizedTest
  @FieldSource("nonNormalNormalisationCases")
  void is_normal_form_returns_false(Pair<String, String> nonNormalNormalisationCases) {
    var nonNormalForm = nonNormalNormalisationCases.getLeft();
    assertThat(Constants.isNormalForm(nonNormalForm, alphanumericDoNotNeedEncoding)).isFalse();
  }

  @ParameterizedTest
  @FieldSource("nonNormalNormalisationCases")
  void normalise_returns_normalised(Pair<String, String> nonNormalNormalisationCases) {
    var nonNormalForm = nonNormalNormalisationCases.getLeft();
    var normalForm = nonNormalNormalisationCases.getRight();
    assertThat(Constants.normalise(nonNormalForm, alphanumericDoNotNeedEncoding))
        .isEqualTo(normalForm);
  }

  @ParameterizedTest
  @FieldSource("normalisationCases")
  void is_normal_form_returns_true_for_normal_form(Pair<String, String> normalisationCase) {
    var normalForm = normalisationCase.getRight();
    assertThat(Constants.isNormalForm(normalForm, alphanumericDoNotNeedEncoding)).isTrue();
  }

  @ParameterizedTest
  @FieldSource("normalisationCases")
  void normalise_returns_null_when_already_normal(Pair<String, String> normalisationCase) {
    var normalForm = normalisationCase.getRight();
    assertThat(Constants.normalise(normalForm, alphanumericDoNotNeedEncoding)).isNull();
  }

  private static final List<Pair<String, String>> encodingCases =
      List.of(
          Pair.of("aßc", "a%C3%9Fc"),
          Pair.of("loC𝐀𝐋𝐇𝐨𝐬𝐭/usr/bin", "loC%F0%9D%90%80%F0%9D%90%8B%F0%9D%90%87%F0%9D%90%A8%F0%9D%90%AC%F0%9D%90%AD%2Fusr%2Fbin")
          );
  @ParameterizedTest
  @FieldSource("encodingCases")
  void encode_encodes_as_expected(Pair<String, String> testCase) {
    assertThat(Constants.encode(testCase.getLeft(), alphanumericDoNotNeedEncoding)).isEqualTo(testCase.getRight());
  }

  @ParameterizedTest
  @FieldSource("encodingCases")
  void decode_decodes_as_expected(Pair<String, String> testCase) {
    assertThat(PercentEncoded.decodeCharacters(testCase.getRight())).isEqualTo(testCase.getLeft());
  }
}
