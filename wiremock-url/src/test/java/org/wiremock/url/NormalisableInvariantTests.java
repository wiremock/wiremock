package org.wiremock.url;

import org.junit.jupiter.api.DynamicTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class NormalisableInvariantTests {

  public static <T extends Normalisable<T>> Stream<DynamicTest> generateNormalisedInvariantTests(
      List<? extends T> normalForms
  ) {
    List<DynamicTest> tests = new ArrayList<>();

    for (Normalisable<T> normalForm : normalForms) {
      tests.add(
          dynamicTest(
              "Already normal : '" + normalForm + "' is in normal form",
              () -> {
                assertThat(normalForm.isNormalForm()).isTrue();

                var normalised = normalForm.normalise();
                assertThat(normalised.isNormalForm()).isTrue();
                assertThat(normalised).isEqualTo(normalForm);
              }));
    }
    return tests.stream();
  }

  public static <T extends Normalisable<T>> Stream<DynamicTest> generateNotNormalisedInvariantTests(
      List<? extends T> notNormalised
  ) {
    List<DynamicTest> tests = new ArrayList<>();

    for (Normalisable<T> normalisable : notNormalised) {
      tests.add(
          dynamicTest(
              "Non-normal : '" + normalisable + "' is not normal form but can be normalised",
              () -> {
                assertThat(normalisable.isNormalForm()).isFalse();

                var normalised = normalisable.normalise();
                assertThat(normalisable).isNotEqualTo(normalised);

                assertThat(normalised.isNormalForm()).isTrue();
                assertThat(normalised.normalise()).isEqualTo(normalised);
              }));
    }
    return tests.stream();
  }
}
