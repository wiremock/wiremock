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

  public record NormalisationCase<T extends Normalisable<T>>(T notNormal, T normalForm) {}

  public static <T extends Normalisable<T>> Stream<DynamicTest> generateNotNormalisedInvariantTests(
      List<NormalisationCase<T>> testCases
  ) {
    List<DynamicTest> tests = new ArrayList<>();

    for (NormalisationCase<T> testCase : testCases) {
      tests.add(
          dynamicTest(
              "Non-normal : `" + testCase.notNormal + "` is not normal form but can be normalised to `" + testCase.normalForm + "`",
              () -> {
                assertThat(testCase.notNormal.isNormalForm()).isFalse();
                assertThat(testCase.normalForm.isNormalForm()).isTrue();

                var normalised = testCase.notNormal.normalise();
                assertThat(normalised).isNotEqualTo(testCase.notNormal);
                assertThat(normalised.isNormalForm()).isTrue();
                assertThat(normalised).isEqualTo(testCase.normalForm);

                assertThat(normalised.normalise()).isEqualTo(normalised);
              }));
    }
    return tests.stream();
  }
}
