package org.wiremock.url;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;
import java.util.List;
import java.util.stream.Stream;

public class OriginTests {


  @Nested
  class Normalise {

    static final List<NormalisationCase<UriReference>> normalisationCases =
        Stream.<Pair<String, String>>of(
            Pair.of("http://example.com", "http://example.com/"),
            Pair.of("http://example.com:8080", "http://example.com:8080/")
        ).map(it ->
            new NormalisationCase<>(
                Origin.parse(it.getLeft()),
                Url.parse(it.getRight())
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
