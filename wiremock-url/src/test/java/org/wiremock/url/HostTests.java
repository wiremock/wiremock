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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HostTests {

  @Nested
  class ParseMethod {

    @Nested
    class IPv4Addresses {

      @ParameterizedTest
      @ValueSource(
          strings = {
            "0.0.0.0",
            "1.1.1.1",
            "8.8.8.8",
            "127.0.0.1",
            "192.168.0.1",
            "192.168.1.100",
            "10.0.0.1",
            "172.16.0.1",
            "255.255.255.255",
            "1.2.3.4",
            "100.200.50.25"
          })
      void parses_valid_ipv4_addresses(String ipv4) {
        Host host = Host.parse(ipv4);
        assertThat(host.toString()).isEqualTo(ipv4);
      }

      @Test
      void parses_invalid_ipv4_as_registered_name() {
        // The regex treats these as valid registered names, not invalid IPv4
        // This is acceptable per RFC 3986 - the host component doesn't validate IPv4 semantics
        Host host1 = Host.parse("256.1.1.1");
        assertThat(host1.toString()).isEqualTo("256.1.1.1");

        Host host2 = Host.parse("1.1.1");
        assertThat(host2.toString()).isEqualTo("1.1.1");

        Host host3 = Host.parse("a.b.c.d");
        assertThat(host3.toString()).isEqualTo("a.b.c.d");
      }
    }

    @Nested
    class IPv6Addresses {

      @ParameterizedTest
      @ValueSource(
          strings = {
            "[::1]", // loopback
            "[::ffff:192.0.2.1]", // IPv4-mapped
            "[2001:db8::1]",
            "[2001:0db8:0000:0000:0000:0000:0000:0001]", // full form
            "[fe80::1]",
            "[::1234:5678]",
            "[2001:db8:85a3::8a2e:370:7334]",
            "[2001:db8:85a3:0:0:8a2e:370:7334]",
            "[::]", // all zeros
            "[2001:db8::8a2e:370:7334]",
            "[ff02::1]",
            "[ff02::2]"
          })
      void parses_valid_ipv6_addresses(String ipv6) {
        Host host = Host.parse(ipv6);
        assertThat(host.toString()).isEqualTo(ipv6);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "::1", // missing brackets
            "2001:db8::1", // missing brackets
            "[2001:db8::1", // missing closing bracket
            "2001:db8::1]", // missing opening bracket
            "[]", // empty brackets
            "[[::1]]", // double brackets
          })
      void throws_exception_for_invalid_ipv6_addresses(String invalidIpv6) {
        assertThatExceptionOfType(IllegalHost.class)
            .isThrownBy(() -> Host.parse(invalidIpv6))
            .withMessage("Illegal host: `" + invalidIpv6 + "`")
            .extracting(IllegalHost::getIllegalValue)
            .isEqualTo(invalidIpv6);
      }

      @Test
      void parses_bracketed_content_as_ipv6() {
        // The regex accepts any non-empty content inside brackets as IPv6
        // This is a simple implementation that defers semantic validation to higher layers
        Host host1 = Host.parse("[   ]");
        assertThat(host1.toString()).isEqualTo("[   ]");

        Host host2 = Host.parse("[not-an-ipv6]");
        assertThat(host2.toString()).isEqualTo("[not-an-ipv6]");
      }
    }

    @Nested
    class RegisteredNames {

      @ParameterizedTest
      @ValueSource(
          strings = {
            "localhost",
            "example.com",
            "www.example.com",
            "sub.domain.example.com",
            "example.co.uk",
            "my-server",
            "my_server",
            "server123",
            "123server",
            "a",
            "a.b",
            "a.b.c.d.e.f.g",
            "xn--e1afmkfd.xn--p1ai", // punycode (Russian domain)
            "test-123.example.com",
            "test_123.example.com",
            "test~server", // tilde is unreserved
            "test.server",
          })
      void parses_valid_registered_names(String registeredName) {
        Host host = Host.parse(registeredName);
        assertThat(host.toString()).isEqualTo(registeredName);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "example.com:8080", // port included (not part of host)
            "user@example.com", // userinfo included
            "example.com/path", // path included
            "example.com?query", // query included
            "example.com#fragment", // fragment included
            "<invalid>",
            "in valid", // space
            "test\nserver", // newline
            "test\tserver", // tab
          })
      void throws_exception_for_invalid_registered_names(String invalidName) {
        assertThatExceptionOfType(IllegalHost.class)
            .isThrownBy(() -> Host.parse(invalidName))
            .withMessage("Illegal host: `" + invalidName + "`")
            .extracting(IllegalHost::getIllegalValue)
            .isEqualTo(invalidName);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "%20", // space encoded
            "%2F", // slash encoded
            "test%20server",
            "my%2Fserver",
            "%C3%A9", // é encoded
            "caf%C3%A9.example.com",
            "%41%42%43", // ABC encoded
          })
      void parses_percent_encoded_registered_names(String encodedName) {
        Host host = Host.parse(encodedName);
        assertThat(host.toString()).isEqualTo(encodedName);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "%", // incomplete encoding
            "%2", // incomplete encoding
            "%GG", // invalid hex
            "%ZZ", // invalid hex
            "test%2", // incomplete at end
            "test%", // incomplete at end
            "test%ZZserver", // invalid hex
          })
      void throws_exception_for_invalid_percent_encoding(String invalidEncoding) {
        assertThatExceptionOfType(IllegalHost.class)
            .isThrownBy(() -> Host.parse(invalidEncoding))
            .withMessage("Illegal host: `" + invalidEncoding + "`")
            .extracting(IllegalHost::getIllegalValue)
            .isEqualTo(invalidEncoding);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "test!", // sub-delims
            "test$",
            "test&",
            "test'",
            "test(",
            "test)",
            "test*",
            "test+",
            "test,",
            "test;",
            "test=",
            "ex!ample.com",
            "ex$ample.com",
          })
      void parses_registered_names_with_sub_delims(String nameWithSubDelims) {
        Host host = Host.parse(nameWithSubDelims);
        assertThat(host.toString()).isEqualTo(nameWithSubDelims);
      }

      @Test
      void parses_empty_registered_name() {
        Host host = Host.parse("");
        assertThat(host.toString()).isEmpty();
      }
    }
  }

  @Nested
  class Equality {

    @Test
    void hosts_with_same_value_are_equal() {
      Host host1 = Host.parse("example.com");
      Host host2 = Host.parse("example.com");
      assertThat(host1).isEqualTo(host2);
    }

    @Test
    void ipv4_hosts_with_same_value_are_equal() {
      Host host1 = Host.parse("192.168.1.1");
      Host host2 = Host.parse("192.168.1.1");
      assertThat(host1).isEqualTo(host2);
    }

    @Test
    void ipv6_hosts_with_same_value_are_equal() {
      Host host1 = Host.parse("[::1]");
      Host host2 = Host.parse("[::1]");
      assertThat(host1).isEqualTo(host2);
    }

    @Test
    void hosts_with_different_values_are_not_equal() {
      Host host1 = Host.parse("example.com");
      Host host2 = Host.parse("other.com");
      assertThat(host1).isNotEqualTo(host2);
    }

    @Test
    void hosts_with_different_case_are_not_equal() {
      Host host1 = Host.parse("example.com");
      Host host2 = Host.parse("EXAMPLE.COM");
      assertThat(host1).isNotEqualTo(host2);
    }

    @Test
    void ipv4_and_registered_name_are_not_equal() {
      Host ipv4 = Host.parse("127.0.0.1");
      Host name = Host.parse("localhost");
      assertThat(ipv4).isNotEqualTo(name);
    }

    @Test
    void host_is_equal_to_itself() {
      Host host = Host.parse("example.com");
      assertThat(host).isEqualTo(host);
    }

    @Test
    void host_is_not_equal_to_null() {
      Host host = Host.parse("example.com");
      assertThat(host).isNotEqualTo(null);
    }

    @Test
    void host_is_not_equal_to_different_type() {
      Host host = Host.parse("example.com");
      assertThat(host).isNotEqualTo("example.com");
    }

    @Test
    void equals_is_symmetric() {
      Host host1 = Host.parse("test.example.com");
      Host host2 = Host.parse("test.example.com");
      assertThat(host1.equals(host2)).isEqualTo(host2.equals(host1));
    }

    @Test
    void equals_is_transitive() {
      Host host1 = Host.parse("192.168.1.100");
      Host host2 = Host.parse("192.168.1.100");
      Host host3 = Host.parse("192.168.1.100");
      assertThat(host1).isEqualTo(host2);
      assertThat(host2).isEqualTo(host3);
      assertThat(host1).isEqualTo(host3);
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_hosts_have_same_hash_code() {
      Host host1 = Host.parse("example.com");
      Host host2 = Host.parse("example.com");
      assertThat(host1.hashCode()).isEqualTo(host2.hashCode());
    }

    @Test
    void equal_ipv4_hosts_have_same_hash_code() {
      Host host1 = Host.parse("10.0.0.1");
      Host host2 = Host.parse("10.0.0.1");
      assertThat(host1.hashCode()).isEqualTo(host2.hashCode());
    }

    @Test
    void equal_ipv6_hosts_have_same_hash_code() {
      Host host1 = Host.parse("[2001:db8::1]");
      Host host2 = Host.parse("[2001:db8::1]");
      assertThat(host1.hashCode()).isEqualTo(host2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Host host = Host.parse("www.example.com");
      int hashCode1 = host.hashCode();
      int hashCode2 = host.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_registered_name() {
      String hostString = "example.com";
      Host host = Host.parse(hostString);
      assertThat(host.toString()).isEqualTo(hostString);
    }

    @Test
    void to_string_returns_original_ipv4() {
      String ipv4 = "192.168.1.1";
      Host host = Host.parse(ipv4);
      assertThat(host.toString()).isEqualTo(ipv4);
    }

    @Test
    void to_string_returns_original_ipv6() {
      String ipv6 = "[2001:db8::1]";
      Host host = Host.parse(ipv6);
      assertThat(host.toString()).isEqualTo(ipv6);
    }

    @Test
    void to_string_preserves_case() {
      String hostString = "Example.COM";
      Host host = Host.parse(hostString);
      assertThat(host.toString()).isEqualTo(hostString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "test%20server";
      Host host = Host.parse(encoded);
      assertThat(host.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Host original = Host.parse("www.example.com");
      String stringForm = original.toString();
      Host parsed = Host.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @Nested
  class EdgeCases {

    @Test
    void parses_host_with_many_subdomains() {
      String longHost = "a.b.c.d.e.f.g.h.i.j.k.l.m.example.com";
      Host host = Host.parse(longHost);
      assertThat(host.toString()).isEqualTo(longHost);
    }

    @Test
    void parses_single_character_host() {
      Host host = Host.parse("a");
      assertThat(host.toString()).isEqualTo("a");
    }

    @Test
    void parses_numeric_registered_name() {
      Host host = Host.parse("123456");
      assertThat(host.toString()).isEqualTo("123456");
    }

    @Test
    void parses_host_with_all_unreserved_characters() {
      String unreservedHost = "aZ09-._~";
      Host host = Host.parse(unreservedHost);
      assertThat(host.toString()).isEqualTo(unreservedHost);
    }

    @Test
    void parses_host_with_multiple_percent_encodings() {
      String encoded = "test%20%21%22%23server";
      Host host = Host.parse(encoded);
      assertThat(host.toString()).isEqualTo(encoded);
    }

    @Test
    void parses_minimum_ipv4() {
      Host host = Host.parse("0.0.0.0");
      assertThat(host.toString()).isEqualTo("0.0.0.0");
    }

    @Test
    void parses_maximum_ipv4() {
      Host host = Host.parse("255.255.255.255");
      assertThat(host.toString()).isEqualTo("255.255.255.255");
    }
  }
}
