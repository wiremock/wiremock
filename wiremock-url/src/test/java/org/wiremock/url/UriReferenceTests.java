/*
 * Copyright (C) 2025 Thomas Akehurst
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase.withContext;
import static org.wiremock.url.whatwg.WhatWGUrlTestManagement.whatwg_valid_wiremock_valid;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;
import org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

public class UriReferenceTests {

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
                UriReference.parse(it.getLeft()),
                UriReference.parse(it.getRight())
            )
        ).toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<UriReference> alreadyNormalisedUriReferences = normalisationCases.stream()
        .map(NormalisationCase::normalForm).distinct().toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(alreadyNormalisedUriReferences);
    }
  }

  @SuppressWarnings("unused")
  private static final List<? extends WhatWGUrlTestCase> wiremock_valid =
      WhatWGUrlTestManagement.wiremock_valid;

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid_has_same_toString(WhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var uriReference = UriReference.parse(input);
          assertThat(uriReference.toString()).isEqualTo(input);
        });
  }

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid_normalise_is_same_instance(WhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var uriReference = UriReference.parse(input);
          UriReference normalised = uriReference.normalise();

          assertThat(normalised.normalise()).isSameAs(normalised);
        });
  }

  private static final List<? extends WhatWGUrlTestCase> wiremock_valid_round_trip =
      WhatWGUrlTestManagement.wiremock_valid.stream()
          .filter(
              testCase -> {
                var uriReference = UriReference.parse(testCase.input());
                var normalised = uriReference.normalise();
                return !(uriReference instanceof PathAndQuery
                        && normalised.toString().startsWith("//"))
                    && !(normalised instanceof OpaqueUri
                        && normalised.getPath().toString().startsWith("//"));
              })
          .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_round_trip")
  void wiremock_valid_normalise_round_trips(WhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var uriReference = UriReference.parse(input);
          UriReference normalised = uriReference.normalise();

          UriReference reconstituted = UriReference.parse(normalised.toString());
          assertThat(reconstituted.getClass()).isEqualTo(normalised.getClass());
          assertThat(reconstituted).isEqualTo(normalised);
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_with_base =
          whatwg_valid_wiremock_valid.stream()
              .filter(t -> t.base() != null && !t.base().isEmpty() && !t.base().equals("sc://ñ"))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_with_base")
  void wiremock_valid_whatwg_success_with_base_has_expected_scheme_and_authority(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var uriReference = UriReference.parse(input);
          assert testCase.base() != null;
          var resolved = Uri.parse(testCase.base()).resolve(uriReference);

          assertThat(resolved.getScheme() + ":").isEqualTo(testCase.protocol());

          Optional<Authority> authority = Optional.ofNullable(resolved.getAuthority());
          Optional<UserInfo> userInfo =
              authority.flatMap(a -> Optional.ofNullable(a.getUserInfo()));
          Optional<Username> username = userInfo.map(UserInfo::getUsername);
          Optional<Password> password = userInfo.flatMap(a -> Optional.ofNullable(a.getPassword()));
          Optional<Port> port = Optional.ofNullable(resolved.getPort());

          assertThat(username.map(Object::toString).orElse("")).isEqualTo(testCase.username());
          assertThat(password.map(Object::toString).orElse("")).isEqualTo(testCase.password());
          assertThat(port.map(Object::toString).orElse("")).isEqualTo(testCase.port());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_with_base_with_correct_path_expectation =
          wiremock_valid_whatwg_success_with_base.stream()
              .filter(
                  t ->
                      // a normalised URI always as a path
                      !t.pathname().isEmpty()
                          // if the input is an Opaque URI the output will be an Opaque URI
                          && !(t.input().matches("^[a-z]+:.*")
                              && !t.input().matches("^[a-z]+://.*")
                              && t.href().matches("^[a-z]+://.*"))
                          // windows style paths
                          && !t.pathname().matches(".*/[a-zA-Z]:(/.*|$)")
                          // % not as part of a percent encoding
                          && !t.pathname().matches(".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)")
                          // whatwg treats this as //test/
                          && !t.input().equals("///test")
                          // whatwg treats this as //example.org/path
                          && !t.input().equals("///example.org/path")
                          // whatwg trims this, we do not
                          && !t.input().equals(" foo.com  ")
                          // whatwg turns this into a single space, we encode it
                          && !t.input().equals("a:\t foo.com")
                          // whatwg turns this into nothing, we encode it
                          && !t.input().equals("  \t")
                          // whatwg turns \ into /, we encode it
                          && !t.input().contains("\\")
                          // whatwg does not encode `|`, we encode it
                          && !t.input().equals("C|a"))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_with_base_with_correct_path_expectation")
  void wiremock_valid_whatwg_success_with_base_has_expected_path(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var base = Uri.parse(testCase.base());
          var resolved = base.resolve(input);

          assertThat(resolved.getPath().toString()).isEqualTo(testCase.pathname());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_with_base_with_correct_search_expectation =
          wiremock_valid_whatwg_success_with_base.stream()
              .filter(
                  t ->
                      // absolute Opaque URI wins
                      !(t.input().equals("file:#x") && t.search().equals("?test"))
                          // absolute Opaque URI wins
                          && !(t.input().equals("file:") && t.search().equals("?test")))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_with_base_with_correct_search_expectation")
  void wiremock_valid_whatwg_success_with_base_has_expected_query(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var base = Uri.parse(testCase.base());
          var resolved = base.resolve(input);

          assertThat(
                  Optional.ofNullable(resolved.getQuery())
                      .map(o -> o.isEmpty() ? "" : "?" + o)
                      .orElse(""))
              .isEqualTo(testCase.search());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_with_base_with_correct_fragment_expectation =
          wiremock_valid_whatwg_success_with_base.stream()
              .filter(
                  t ->
                      // we do not trim the end of the fragment
                      !t.input().equals("http://f:21/ b ? d # e ")
                          // we encode `\`
                          && !t.input().equals("#\\")
                          // we encode `#`
                          && !t.input().equals("http://foo/path;a??e#f#g"))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_with_base_with_correct_fragment_expectation")
  void wiremock_valid_whatwg_success_with_base_has_expected_fragment(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var base = Uri.parse(testCase.base());
          var resolved = base.resolve(input);

          assertThat(
                  Optional.ofNullable(resolved.getFragment())
                      .map(o -> o.isEmpty() ? "" : "#" + o)
                      .orElse(""))
              .isEqualTo(testCase.hash());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_without_base =
          whatwg_valid_wiremock_valid.stream()
              .filter(t -> t.base() == null || t.base().isEmpty())
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_without_base")
  void wiremock_valid_whatwg_success_without_base_has_expected_scheme(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var resolved = UriReference.parse(input).normalise();

          assertThat(resolved.getScheme() + ":").isEqualTo(testCase.protocol());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_without_base_with_valid_authority =
          wiremock_valid_whatwg_success_without_base.stream()
              .filter(
                  t ->
                      !(t.input().startsWith("http:a") && t.href().startsWith("http://"))
                          && !(t.input().startsWith("http::") && t.href().startsWith("http://"))
                          && !(t.input().startsWith("http:/:") && t.href().startsWith("http://"))
                          && !(t.input().startsWith("http:/a") && t.href().startsWith("http://")))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_without_base_with_valid_authority")
  void wiremock_valid_whatwg_success_without_base_has_expected_authority(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = testCase.input();
          var resolved = UriReference.parse(input).normalise();

          Optional<UserInfo> userInfo = Optional.ofNullable(resolved.getUserInfo());
          Optional<Username> username = userInfo.map(UserInfo::getUsername);
          Optional<Password> password = userInfo.flatMap(a -> Optional.ofNullable(a.getPassword()));
          Optional<Port> port = Optional.ofNullable(resolved.getPort());

          assertThat(username.map(Object::toString).orElse("")).isEqualTo(testCase.username());
          assertThat(password.map(Object::toString).orElse("")).isEqualTo(testCase.password());
          assertThat(port.map(Object::toString).orElse("")).isEqualTo(testCase.port());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_without_base_with_correct_path_expectation =
          wiremock_valid_whatwg_success_without_base.stream()
              .filter(
                  t ->
                      // a normalised URI always has a path
                      !t.pathname().isEmpty()
                          // if the input is an Opaque URI the output will be an Opaque URI
                          && !(t.input().matches("^[a-z]+:.*")
                              && !t.input().matches("^[a-z]+://.*")
                              && t.href().matches("^[a-z]+://.*"))
                          // windows style paths
                          && !t.pathname().matches(".*/[a-zA-Z]:(/.*|$)")
                          // % not as part of a percent encoding
                          && !t.pathname().matches(".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)")
                          // whatwg turns \ into /, we encode it
                          && !t.input().contains("\\")
                          // we encode spaces
                          && !t.pathname().contains(" ")
                          // we encode pipes
                          && !t.pathname().contains("|")
                          // whatwg turns tab into space, we encode it
                          && !t.input().equals("http://example.com/foo\tbar")
                          // whatwg trims trailing spaces, we encode them
                          && !t.input().equals("non-special:opaque  "))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_without_base_with_correct_path_expectation")
  void wiremock_valid_whatwg_success_without_base_has_expected_path(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var resolved = input.normalise();

          assertThat(resolved.getPath().toString()).isEqualTo(testCase.pathname());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_without_base_with_correct_query_expectation =
          wiremock_valid_whatwg_success_without_base.stream()
              .filter(
                  t ->
                      // we encode a percent that is not part of a percent encoded hex value
                      !t.input().equals("http://example.org/test?%GH")
                          // we encode more of the query than whatwgurl
                          && !t.input().equals("foo://host/dir/? !\"$%&'()*+,-./:;<=>?@[\\]^_`{|}~")
                          // we encode more of the query than whatwgurl
                          && !t.input()
                              .equals("wss://host/dir/? !\"$%&'()*+,-./:;<=>?@[\\]^_`{|}~"))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_without_base_with_correct_query_expectation")
  void wiremock_valid_whatwg_success_without_base_has_expected_query(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var resolved = input.normalise();

          assertThat(
                  Optional.ofNullable(resolved.getQuery())
                      .map(o -> o.isEmpty() ? "" : "?" + o)
                      .orElse(""))
              .isEqualTo(testCase.search());
        });
  }

  private static final List<? extends SuccessWhatWGUrlTestCase>
      wiremock_valid_whatwg_success_without_base_with_correct_fragment_expectation =
          wiremock_valid_whatwg_success_without_base.stream()
              .filter(
                  t ->
                      // we encode a percent that is not part of a percent encoded hex value
                      !t.input().equals("http://example.org/test?a#%GH")
                          // we encode much more than whatwgurl
                          && !t.input()
                              .equals("foo://host/dir/# !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~")
                          && !t.input()
                              .equals("wss://host/dir/# !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"))
              .toList();

  @ParameterizedTest
  @FieldSource("wiremock_valid_whatwg_success_without_base_with_correct_fragment_expectation")
  void wiremock_valid_whatwg_success_without_base_has_expected_fragment(
      SuccessWhatWGUrlTestCase testCase) {
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          var resolved = input.normalise();

          assertThat(
                  Optional.ofNullable(resolved.getFragment())
                      .map(o -> o.isEmpty() ? "" : "#" + o)
                      .orElse(""))
              .isEqualTo(testCase.hash());
        });
  }

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    var input = testCase.input();
    var uriReference = UriReference.parse(input);

    UriReference normalised = uriReference.normalise();

    if (testCase instanceof SuccessWhatWGUrlTestCase successTestCase) {
      UriReference resolved;
      if (successTestCase.base() != null) {
        resolved = resolve(uriReference, successTestCase.base());
      } else {
        resolved = normalised;
      }

      Optional<Authority> authority = Optional.ofNullable(resolved.getAuthority());

      if (Optional.ofNullable(resolved.getHost())
              .map(Object::toString)
              .orElse("")
              .equals(successTestCase.hostname())
          && !successTestCase.pathname().isEmpty()
          && !successTestCase.pathname().matches(".*/[a-zA-Z]:(/.*|$)")
          && !successTestCase
              .pathname()
              .matches(
                  ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
          && !input.endsWith(" ")
          && !resolved.getPath().toString().contains("\\")
          && !successTestCase.pathname().contains("|")
          && !uriReference.getPath().toString().contains("\t")
          && !successTestCase.search().contains("{")
          && !successTestCase
              .search()
              .matches(
                  ".*%[a-fA-F0-9]?(?:[^a-fA-F0-9].*|$)") // % not as part of a percent encoding
          && !successTestCase.hash().contains("{")
          && !successTestCase.hash().contains("#")) {
        assertThat(resolved.toString()).isEqualTo(successTestCase.href());
        assertThat(authority.map(Authority::getHostAndPort).map(Object::toString).orElse(""))
            .isEqualTo(successTestCase.host());
      }
    }
  }

  private static Uri resolve(UriReference urlReference, String baseString) {
    return Url.parse(baseString).resolve(urlReference);
  }

  @SuppressWarnings("unused")
  private static final List<? extends WhatWGUrlTestCase> wiremock_invalid =
      WhatWGUrlTestManagement.wiremock_invalid;

  @ParameterizedTest
  @FieldSource("wiremock_invalid")
  void wiremock_invalid(WhatWGUrlTestCase testCase) {
    System.out.println(testCase);
    assertThatExceptionOfType(IllegalUriReference.class)
        .isThrownBy(() -> UriReference.parse(testCase.input()));
  }

  @Test
  void relativeRefParserThrowsCorrectExceptionType() {
    // When parsing a URL (not a relative reference), RelativeRef.parse should throw
    // IllegalRelativeRef, not IllegalOrigin
    assertThatExceptionOfType(IllegalRelativeRef.class)
        .isThrownBy(() -> RelativeRef.parse("http://example.com/"));
  }

  // convenience way to test specific cases
  @Test
  @Disabled
  void debug() {
    var testCase =
        new SuccessWhatWGUrlTestCase(
            /* input */ ":foo.com\\",
            /* base */ "http://example.org/foo/bar",
            /* href */ "http://example.org/foo/:foo.com/",
            /* origin */ "http://example.org",
            /* protocol */ "http:",
            /* username */ "",
            /* password */ "",
            /* host */ "example.org",
            /* hostname */ "example.org",
            /* port */ "",
            /* pathname */ "/foo/:foo.com/",
            /* search */ "",
            /* searchParams */ null,
            /* hash */ "");
    withContext(
        testCase,
        () -> {
          var input = UriReference.parse(testCase.input());
          report("input", input);
          assert testCase.base() != null;
          Uri base = Uri.parse(testCase.base());
          report("base", base);
          var resolved = base.resolve(input);
          report("resolved", resolved);

          assertThat(resolved.getPath().toString()).isEqualTo(testCase.pathname());
        });
  }

  private static void report(String element, UriReference uriReference) {
    System.out.println(element + ": " + uriReference.getClass() + " `" + uriReference + "`");
  }
}
