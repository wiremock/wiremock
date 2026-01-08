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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;

public class NormalisableInvariantTests {

  public static <T extends Normalisable<T>> Stream<DynamicTest> generateNormalisedInvariantTests(
      Collection<? extends T> normalForms) {
    List<DynamicTest> tests = new ArrayList<>();

    for (Normalisable<T> normalForm : normalForms) {
      tests.add(
          dynamicTest(
              "Already normal : '" + normalForm + "' is in normal form",
              () -> {
                assertThat(normalForm.isNormalForm()).describedAs(normalForm.toString()).isTrue();

                var normalised = normalForm.normalise();
                assertThat(normalised.isNormalForm()).describedAs(normalised.toString()).isTrue();
                assertThat(normalised).isEqualTo(normalForm);
              }));
    }
    return tests.stream();
  }

  public record NormalisationCase<T extends Normalisable<T>>(T notNormal, T normalForm) {}

  public static <T extends Normalisable<T>> Stream<DynamicTest> generateNotNormalisedInvariantTests(
      List<NormalisationCase<T>> testCases) {
    List<DynamicTest> tests = new ArrayList<>();

    for (NormalisationCase<T> testCase : testCases) {
      T notNormal = testCase.notNormal;
      T normalForm = testCase.normalForm;
      tests.add(
          dynamicTest(
              "Non-normal : `"
                  + notNormal
                  + "` is not normal form but can be normalised to `"
                  + normalForm
                  + "`",
              () -> {
                assertThat(notNormal.isNormalForm()).describedAs(notNormal.toString()).isFalse();
                assertThat(normalForm.isNormalForm()).describedAs(normalForm.toString()).isTrue();

                var normalised = notNormal.normalise();
                assertThat(normalised).isNotEqualTo(notNormal);
                assertThat(normalised.isNormalForm()).describedAs(normalised.toString()).isTrue();
                assertThat(normalised).isEqualTo(normalForm);

                assertThat(normalised.normalise()).isEqualTo(normalised);

                // check that the optimisations have not changed anything
                assertThat(notNormal.isNormalForm()).describedAs(notNormal.toString()).isFalse();
                assertThat(normalised.isNormalForm()).describedAs(normalised.toString()).isTrue();
                assertThat(normalForm.isNormalForm()).describedAs(normalForm.toString()).isTrue();
              }));
    }
    return tests.stream();
  }
}
