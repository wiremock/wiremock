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
      Collection<? extends T> normalForms
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
      T notNormal = testCase.notNormal;
      T normalForm = testCase.normalForm;
      tests.add(
          dynamicTest(
              "Non-normal : `" + notNormal + "` is not normal form but can be normalised to `" + normalForm
                  + "`",
              () -> {
                assertThat(notNormal.isNormalForm()).isFalse();
                assertThat(normalForm.isNormalForm()).isTrue();

                var normalised = notNormal.normalise();
                assertThat(normalised).isNotEqualTo(notNormal);
                assertThat(normalised.isNormalForm()).isTrue();
                assertThat(normalised).isEqualTo(normalForm);

                assertThat(normalised.normalise()).isEqualTo(normalised);

                // check that the optimisations have not changed anything
                assertThat(notNormal.isNormalForm()).isFalse();
                assertThat(normalised.isNormalForm()).isTrue();
                assertThat(normalForm.isNormalForm()).isTrue();
              }));
    }
    return tests.stream();
  }
}
