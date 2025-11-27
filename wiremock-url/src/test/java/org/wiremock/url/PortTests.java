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

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PortTests {

  @Nested
  class OfMethod {

    @ParameterizedTest
    @ValueSource(ints = {1, 80, 443, 8080, 8443, 9000, 65535})
    void creates_ports_with_various_valid_values(int portNumber) {
      Port port = Port.of(portNumber);
      assertThat(port.port()).isEqualTo(portNumber);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1000, -1, 0, 65536, 70000, 100000})
    void throws_exception_for_various_invalid_ports(int invalidPort) {
      assertThatExceptionOfType(IllegalPort.class)
          .isThrownBy(() -> Port.of(invalidPort))
          .withMessage(
              "Illegal port ["
                  + invalidPort
                  + "]; Port value must be an integer between 1 and 65535")
          .withNoCause()
          .extracting(IllegalPort::getIllegalValue)
          .isEqualTo(String.valueOf(invalidPort));
    }
  }

  @Nested
  class ParseMethod {

    @Test
    void uri() {
      var uri = URI.create("http://localhost:00080");
      assertThat(uri.getPort()).isEqualTo(80);
      assertThat(uri.toString()).isEqualTo("http://localhost:00080");
    }

    static Stream<String> validPortStrings() {
      return Stream.of("1", "80", "443", "8080", "8443", "9000", "65535", "00080");
    }

    @ParameterizedTest
    @MethodSource("validPortStrings")
    void parses_various_valid_port_strings(String portString) {
      Port port = Port.parse(portString);
      assertThat(port.port()).isEqualTo(Integer.parseInt(portString));
      assertThat(port.toString()).isEqualTo(portString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "-1000", "-1", "0", "65536", "70000", "100000",
        })
    void throws_exception_for_strings_in_invalid_range(String invalidPortString) {
      assertThatExceptionOfType(IllegalPort.class)
          .isThrownBy(() -> Port.parse(invalidPortString))
          .withMessage(
              "Illegal port ["
                  + invalidPortString
                  + "]; Port value must be an integer between 1 and 65535")
          .withNoCause()
          .extracting(IllegalPort::getIllegalValue)
          .isEqualTo(invalidPortString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "+80",
          "abc",
          "80.0",
          "12.34",
          "1e3",
          "0x50",
          "",
          "   ",
          " 80",
          "80 ",
          " 80 ",
          "port8080",
          "8080port",
          "80 80"
        })
    void throws_exception_for_various_invalid_strings(String invalidPortString) {
      assertThatExceptionOfType(IllegalPort.class)
          .isThrownBy(() -> Port.parse(invalidPortString))
          .withMessage(
              "Illegal port ["
                  + invalidPortString
                  + "]; Port value must be an integer between 1 and 65535")
          .withNoCause()
          .extracting(IllegalPort::getIllegalValue)
          .isEqualTo(String.valueOf(invalidPortString));
    }
  }

  @Nested
  class Equality {

    @Test
    void ports_with_same_value_are_equal() {
      Port port1 = Port.of(8080);
      Port port2 = Port.of(8080);
      assertThat(port1).isEqualTo(port2);
    }

    @Test
    void ports_created_with_same_canonical_string_are_equal() {
      Port portFromInt = Port.of(8080);
      Port portFromString = Port.parse("8080");
      assertThat(portFromInt).isEqualTo(portFromString);
      assertThat(portFromInt.toString()).isEqualTo("8080");
      assertThat(portFromString.toString()).isEqualTo("8080");
    }

    @Test
    void ports_with_different_string_representations_are_not_equal() {
      Port portWithLeadingZeros = Port.parse("00080");
      Port portCanonical = Port.of(80);
      assertThat(portWithLeadingZeros.port()).isEqualTo(80);
      assertThat(portCanonical.port()).isEqualTo(80);
      assertThat(portWithLeadingZeros).isNotEqualTo(portCanonical);
      assertThat(portWithLeadingZeros.toString()).isEqualTo("00080");
      assertThat(portCanonical.toString()).isEqualTo("80");
    }

    @Test
    void ports_with_different_values_are_not_equal() {
      Port port1 = Port.of(8080);
      Port port2 = Port.of(9000);
      assertThat(port1).isNotEqualTo(port2);
    }

    @Test
    void port_is_equal_to_itself() {
      Port port = Port.of(8080);
      assertThat(port).isEqualTo(port);
    }

    @Test
    void port_is_not_equal_to_null() {
      Port port = Port.of(8080);
      assertThat(port).isNotEqualTo(null);
    }

    @Test
    void port_is_not_equal_to_different_type() {
      Port port = Port.of(8080);
      assertThat(port).isNotEqualTo("8080");
      assertThat(port).isNotEqualTo(8080);
    }

    @Test
    void equals_is_symmetric() {
      Port port1 = Port.of(443);
      Port port2 = Port.of(443);
      assertThat(port1.equals(port2)).isEqualTo(port2.equals(port1));
    }

    @Test
    void equals_is_transitive() {
      Port port1 = Port.of(443);
      Port port2 = Port.of(443);
      Port port3 = Port.of(443);
      assertThat(port1).isEqualTo(port2);
      assertThat(port2).isEqualTo(port3);
      assertThat(port1).isEqualTo(port3);
    }
  }

  @Nested
  class HashCode {

    @Test
    void hash_code_based_on_string_representation() {
      Port port = Port.of(8080);
      assertThat(port.hashCode()).isEqualTo("8080".hashCode());
    }

    @Test
    void hash_code_for_port_with_leading_zeros() {
      Port port = Port.parse("00080");
      assertThat(port.hashCode()).isEqualTo("00080".hashCode());
    }

    @Test
    void equal_ports_have_same_hash_code() {
      Port port1 = Port.of(443);
      Port port2 = Port.of(443);
      assertThat(port1.hashCode()).isEqualTo(port2.hashCode());
    }

    @Test
    void ports_with_different_string_representations_have_different_hash_codes() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.of(80);
      assertThat(port1.hashCode()).isNotEqualTo(port2.hashCode());
    }

    @Test
    void different_ports_have_different_hash_codes() {
      Port port1 = Port.of(8080);
      Port port2 = Port.of(9000);
      assertThat(port1.hashCode()).isNotEqualTo(port2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Port port = Port.of(8443);
      int hashCode1 = port.hashCode();
      int hashCode2 = port.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @ParameterizedTest
    @ValueSource(ints = {1, 80, 443, 8080, 8443, 9000, 65535})
    void to_string_returns_correct_string_for_various_ports(int portNumber) {
      Port port = Port.of(portNumber);
      assertThat(port.toString()).isEqualTo(String.valueOf(portNumber));
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Port original = Port.of(8080);
      String stringForm = original.toString();
      Port parsed = Port.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }

    @Test
    void to_string_preserves_leading_zeros() {
      Port port = Port.parse("00080");
      assertThat(port.toString()).isEqualTo("00080");
    }

    @Test
    void to_string_preserves_format_from_parse() {
      assertThat(Port.parse("00080").toString()).isEqualTo("00080");
      assertThat(Port.parse("0080").toString()).isEqualTo("0080");
      assertThat(Port.parse("080").toString()).isEqualTo("080");
      assertThat(Port.parse("80").toString()).isEqualTo("80");
    }
  }

  @Nested
  class LeadingZeros {

    @ParameterizedTest
    @ValueSource(strings = {"00080", "0080", "080", "00443", "01", "001", "0001", "00001"})
    void parses_ports_with_leading_zeros(String portString) {
      Port port = Port.parse(portString);
      assertThat(port.port()).isEqualTo(Integer.parseInt(portString));
      assertThat(port.toString()).isEqualTo(portString);
    }

    @Test
    void ports_with_leading_zeros_have_correct_port_number() {
      Port port = Port.parse("00080");
      assertThat(port.port()).isEqualTo(80);
    }

    @Test
    void ports_with_and_without_leading_zeros_are_not_equal() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.parse("080");
      Port port3 = Port.parse("80");
      Port port4 = Port.of(80);

      assertThat(port1).isNotEqualTo(port2);
      assertThat(port1).isNotEqualTo(port3);
      assertThat(port1).isNotEqualTo(port4);
      assertThat(port2).isNotEqualTo(port3);
      assertThat(port2).isNotEqualTo(port4);
      assertThat(port3).isEqualTo(port4); // These are equal - same string representation
    }

    @Test
    void ports_with_same_leading_zeros_are_equal() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.parse("00080");
      assertThat(port1).isEqualTo(port2);
    }

    @Test
    void leading_zeros_are_preserved_in_round_trip() {
      String original = "00443";
      Port port = Port.parse(original);
      String stringForm = port.toString();
      Port reparsed = Port.parse(stringForm);
      assertThat(stringForm).isEqualTo(original);
      assertThat(reparsed).isEqualTo(port);
      assertThat(reparsed.toString()).isEqualTo(original);
    }

    @Test
    void port_with_many_leading_zeros() {
      Port port = Port.parse("000000080");
      assertThat(port.port()).isEqualTo(80);
      assertThat(port.toString()).isEqualTo("000000080");
    }

    @Test
    void leading_zeros_do_not_affect_port_number() {
      assertThat(Port.parse("00080").port()).isEqualTo(80);
      assertThat(Port.parse("0080").port()).isEqualTo(80);
      assertThat(Port.parse("080").port()).isEqualTo(80);
      assertThat(Port.parse("80").port()).isEqualTo(80);
      assertThat(Port.of(80).port()).isEqualTo(80);
    }

    @Test
    void ports_with_leading_zeros_are_not_cached() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.parse("00080");
      // Non-canonical ports are not cached, so different instances
      assertThat(port1).isNotSameAs(port2);
      // But they are equal (same string representation)
      assertThat(port1).isEqualTo(port2);
    }

    @Test
    void canonical_ports_are_cached() {
      Port port1 = Port.parse("80");
      Port port2 = Port.parse("80");
      Port port3 = Port.of(80);
      // Canonical ports are cached, so same instance
      assertThat(port1).isSameAs(port2);
      assertThat(port1).isSameAs(port3);
    }

    @Test
    void mixed_canonical_and_non_canonical_not_cached_together() {
      Port canonical = Port.parse("80");
      Port withLeadingZeros = Port.parse("00080");
      // Different instances (canonical cached, non-canonical not)
      assertThat(canonical).isNotSameAs(withLeadingZeros);
      // Different values (different string representations)
      assertThat(canonical).isNotEqualTo(withLeadingZeros);
    }
  }

  @Nested
  class Normalise {

    @Test
    void normalise_removes_leading_zeros() {
      Port portWithLeadingZeros = Port.parse("00080");
      Port normalised = portWithLeadingZeros.normalise();
      assertThat(normalised.toString()).isEqualTo("80");
      assertThat(normalised.port()).isEqualTo(80);
    }

    @Test
    void normalise_returns_equal_to_port_of() {
      Port portWithLeadingZeros = Port.parse("00080");
      Port normalised = portWithLeadingZeros.normalise();
      Port canonical = Port.of(80);
      assertThat(normalised).isEqualTo(canonical);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00080", "0080", "080", "00443", "01", "001", "0001", "00001"})
    void normalise_removes_all_leading_zeros(String portString) {
      Port port = Port.parse(portString);
      Port normalised = port.normalise();
      int expectedPort = Integer.parseInt(portString);
      assertThat(normalised.toString()).isEqualTo(String.valueOf(expectedPort));
      assertThat(normalised.port()).isEqualTo(expectedPort);
    }

    @Test
    void normalise_on_canonical_form_returns_equal_port() {
      Port canonical = Port.of(8080);
      Port normalised = canonical.normalise();
      assertThat(normalised).isEqualTo(canonical);
      assertThat(normalised.toString()).isEqualTo("8080");
    }

    @Test
    void normalise_on_parsed_canonical_form_returns_equal_port() {
      Port parsed = Port.parse("443");
      Port normalised = parsed.normalise();
      assertThat(normalised).isEqualTo(parsed);
      assertThat(normalised.toString()).isEqualTo("443");
    }

    @Test
    void normalise_is_idempotent() {
      Port port = Port.parse("00080");
      Port normalised1 = port.normalise();
      Port normalised2 = normalised1.normalise();
      assertThat(normalised1).isEqualTo(normalised2);
      assertThat(normalised1.toString()).isEqualTo("80");
      assertThat(normalised2.toString()).isEqualTo("80");
    }

    @Test
    void normalise_allows_comparison_of_ports_with_different_formats() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.parse("080");
      Port port3 = Port.parse("80");
      Port port4 = Port.of(80);

      // Original ports are not equal
      assertThat(port1).isNotEqualTo(port2);
      assertThat(port1).isNotEqualTo(port3);
      assertThat(port1).isNotEqualTo(port4);

      // Normalised ports are all equal
      assertThat(port1.normalise()).isEqualTo(port2.normalise());
      assertThat(port1.normalise()).isEqualTo(port3.normalise());
      assertThat(port1.normalise()).isEqualTo(port4.normalise());
    }

    @Test
    void normalise_preserves_port_number() {
      Port port = Port.parse("00443");
      Port normalised = port.normalise();
      assertThat(port.port()).isEqualTo(normalised.port());
      assertThat(normalised.port()).isEqualTo(443);
    }

    @Test
    void normalise_creates_cacheable_instance() {
      Port port1 = Port.parse("00080");
      Port port2 = Port.parse("0080");
      Port normalised1 = port1.normalise();
      Port normalised2 = port2.normalise();

      // Normalised instances should be the same cached instance
      assertThat(normalised1).isSameAs(normalised2);
      assertThat(normalised1).isSameAs(Port.of(80));
    }

    @Test
    void normalise_minimum_port() {
      Port port = Port.parse("01");
      Port normalised = port.normalise();
      assertThat(normalised.toString()).isEqualTo("1");
      assertThat(normalised).isEqualTo(Port.of(1));
    }

    @Test
    void normalise_maximum_port() {
      Port port = Port.parse("065535");
      Port normalised = port.normalise();
      assertThat(normalised.toString()).isEqualTo("65535");
      assertThat(normalised).isEqualTo(Port.of(65535));
    }

    @Test
    void normalise_with_many_leading_zeros() {
      Port port = Port.parse("000000080");
      Port normalised = port.normalise();
      assertThat(normalised.toString()).isEqualTo("80");
      assertThat(normalised.port()).isEqualTo(80);
    }

    @TestFactory
    Stream<DynamicTest> port_parser_invariants() {
      return CharSequenceParserInvariantTests.generateInvariantTests(
          PortParser.INSTANCE, ParseMethod.validPortStrings().toList());
    }
  }
}
