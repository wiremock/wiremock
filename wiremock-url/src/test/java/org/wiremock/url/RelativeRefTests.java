package org.wiremock.url;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

class RelativeRefTests {

  @Nested
  class Normalise {

    static final List<NormalisationCase<UriReference>> normalisationCases =
        Stream.of(
            // Protocol-relative URLs - host normalization
            Pair.of("//EXAMPLE.COM:8080", "//example.com:8080/"),
            Pair.of("//EXAMPLE.COM:08080", "//example.com:8080/"),
            Pair.of("//example.com:08080", "//example.com:8080/")
        ).map(it ->
            new NormalisationCase<>(
                RelativeRef.parse(it.getLeft()),
                RelativeRef.parse(it.getRight())
            )
        ).toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<UriReference> alreadyNormalisedUrlReferences = normalisationCases.stream()
        .map(NormalisationCase::normalForm).distinct().toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(alreadyNormalisedUrlReferences);
    }
  }


}