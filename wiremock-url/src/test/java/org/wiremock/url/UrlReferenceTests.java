package org.wiremock.url;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;
import java.util.List;
import java.util.stream.Stream;

class UrlReferenceTests {

  @Nested
  class Normalise {

    static final List<NormalisationCase<UriReference>> normalisationCases =
        Stream.<Pair<String, String>>of(
            // Scheme normalization - uppercase to lowercase
            Pair.of("HTTPS://EXAMPLE.COM:8080", "https://example.com:8080/"),
            Pair.of("HTTPS://EXAMPLE.COM:08080", "https://example.com:8080/"),
            Pair.of("HTTPS://example.com:08080", "https://example.com:8080/"),
            Pair.of("HTTPS://example.com:8080", "https://example.com:8080/"),
            Pair.of("HTTP://example.com", "http://example.com/"),
            Pair.of("FTP://example.com", "ftp://example.com/"),

            // Host normalization - uppercase to lowercase
            Pair.of("https://EXAMPLE.COM:8080", "https://example.com:8080/"),
            Pair.of("https://EXAMPLE.COM:08080", "https://example.com:8080/"),
            Pair.of("http://WWW.EXAMPLE.COM", "http://www.example.com/"),
            Pair.of("http://Example.Com", "http://example.com/"),

            // Port normalization - leading zeros
            Pair.of("https://example.com:08080", "https://example.com:8080/"),
            Pair.of("http://example.com:09090", "http://example.com:9090/"),
            Pair.of("http://example.com:00080", "http://example.com/"),

            // Port normalization - default port removal
            Pair.of("http://example.com:80", "http://example.com/"),
            Pair.of("http://example.com:80/", "http://example.com/"),
            Pair.of("http://example.com:80/path", "http://example.com/path"),
            Pair.of("http://example.com:080", "http://example.com/"),
            Pair.of("https://example.com:443", "https://example.com/"),
            Pair.of("https://example.com:443/", "https://example.com/"),
            Pair.of("https://example.com:443/path", "https://example.com/path"),
            Pair.of("https://example.com:0443", "https://example.com/"),

            // Protocol-relative URLs - host normalization
            Pair.of("//EXAMPLE.COM:8080", "//example.com:8080/"),
            Pair.of("//EXAMPLE.COM:08080", "//example.com:8080/"),
            Pair.of("//example.com:08080", "//example.com:8080/"),

            // Percent encoding - uppercase hex digits in path
            Pair.of("http://example.com/%1f", "http://example.com/%1F"),
            Pair.of("http://example.com/%1f%3f", "http://example.com/%1F%3F"),
            Pair.of("http://example.com/path%1fto", "http://example.com/path%1Fto"),
            Pair.of("http://example.com/%3f%3F", "http://example.com/%3F%3F"),
            Pair.of("http://example.com/%ab%cd%ef", "http://example.com/%AB%CD%EF"),

            // Percent encoding - decode unreserved characters in path (A-Z a-z 0-9 - . _ ~)
            Pair.of("http://example.com/%41", "http://example.com/A"),
            Pair.of("http://example.com/%61", "http://example.com/a"),
            Pair.of("http://example.com/%30", "http://example.com/0"),
            Pair.of("http://example.com/%7E", "http://example.com/~"),
            Pair.of("http://example.com/%7e", "http://example.com/~"),
            Pair.of("http://example.com/%2D", "http://example.com/-"),
            Pair.of("http://example.com/%2E", "http://example.com/"),
            Pair.of("http://example.com/%5F", "http://example.com/_"),
            Pair.of("http://example.com/%41%42%43", "http://example.com/ABC"),
            Pair.of("http://example.com/~%75ser", "http://example.com/~user"),

            // Percent encoding - uppercase hex in query
            Pair.of("http://example.com?key=%1f", "http://example.com/?key=%1F"),
            Pair.of("http://example.com?a=%1f&b=%1a", "http://example.com/?a=%1F&b=%1A"),
            Pair.of("http://example.com?key=%ab", "http://example.com/?key=%AB"),

            // Percent encoding - decode unreserved in query
            Pair.of("http://example.com?key=%41", "http://example.com/?key=A"),
            Pair.of("http://example.com?%61=%62", "http://example.com/?a=b"),
            Pair.of("http://example.com?key=%7E", "http://example.com/?key=~"),

            // Percent encoding - uppercase hex in fragment
            Pair.of("http://example.com#%1f", "http://example.com/#%1F"),
            Pair.of("http://example.com#%ab", "http://example.com/#%AB"),

            // Percent encoding - decode unreserved in fragment
            Pair.of("http://example.com#%41", "http://example.com/#A"),
            Pair.of("http://example.com#%7E", "http://example.com/#~"),
            Pair.of("http://example.com#%61%62%63", "http://example.com/#abc"),

            // Combined normalizations - scheme + host + port
            Pair.of("HTTP://EXAMPLE.COM:80", "http://example.com/"),
            Pair.of("HTTPS://EXAMPLE.COM:443", "https://example.com/"),
            Pair.of("HTTP://EXAMPLE.COM:080", "http://example.com/"),
            Pair.of("HTTPS://EXAMPLE.COM:0443", "https://example.com/"),

            // Combined normalizations - multiple components
            Pair.of("HTTP://EXAMPLE.COM:80/%1f", "http://example.com/%1F"),
            Pair.of("HTTPS://EXAMPLE.COM:443/PATH", "https://example.com/PATH"),
            Pair.of("HTTP://EXAMPLE.COM/%41%42", "http://example.com/AB"),
            Pair.of("HTTPS://EXAMPLE.COM:0443/path?key=%41", "https://example.com/path?key=A"),
            Pair.of("HTTP://EXAMPLE.COM:080/%1f?a=%1f#%1f", "http://example.com/%1F?a=%1F#%1F"),
            Pair.of("HTTPS://EXAMPLE.COM:443/%61?%62=%63#%64", "https://example.com/a?b=c#d"),

            // Path with percent encoding variations
            Pair.of("http://example.com/%41/%42/%43", "http://example.com/A/B/C"),
            Pair.of("http://example.com/path/%1F/segment", "http://example.com/path/%1F/segment"),
            Pair.of("http://example.com/%7Euser/docs", "http://example.com/~user/docs"),

            // Query and fragment combinations
            Pair.of("http://example.com?%41=%42#%43", "http://example.com/?A=B#C"),
            Pair.of("http://example.com?key=%1f#%1f", "http://example.com/?key=%1F#%1F"),

            // Multiple ports in different contexts
            Pair.of("http://example.com:8080", "http://example.com:8080/"),
            Pair.of("https://example.com:8443", "https://example.com:8443/"),
            Pair.of("ftp://example.com:21", "ftp://example.com/"),

            // Mixed case hex digits
            Pair.of("http://example.com/%aB%Cd", "http://example.com/%AB%CD"),
            Pair.of("http://example.com?key=%aB", "http://example.com/?key=%AB"),
            Pair.of("http://example.com#%aB", "http://example.com/#%AB")
        ).map(it ->
            new NormalisationCase<>(
                UrlReference.parse(it.getLeft()),
                UrlReference.parse(it.getRight())
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