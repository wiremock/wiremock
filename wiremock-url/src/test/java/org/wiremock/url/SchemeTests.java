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

class SchemeTests {

  @Nested
  class OfMethod {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "http", "https", "ftp", "ssh", "file", "mailto",
          "HTTP", "HTTPS", "FTP", "SSH", "FILE", "MAILTO",
          "Http", "Https", "Ftp", "Ssh", "File", "Mailto",
          "svn+ssh", "content-type", "x.custom", "h2c", "custom+proto-1.0"
        })
    void parses_valid_schemes(String schemeString) {
      Scheme scheme = Scheme.of(schemeString);
      assertThat(scheme.toString()).isEqualTo(schemeString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"h", "a", "z", "A", "Z"})
    void parses_single_letter_schemes(String schemeString) {
      Scheme scheme = Scheme.of(schemeString);
      assertThat(scheme.toString()).isEqualTo(schemeString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "-http",
          "+http",
          ".http",
          "",
          "http!",
          "http@home",
          "http#tag",
          "http$var",
          "http%20",
          "http&amp",
          "http:",
          "http://",
          "://",
          " http",
          "http ",
          " http "
        })
    void throws_exception_for_illegal_schemes(String illegalScheme) {
      assertThatExceptionOfType(IllegalScheme.class)
          .isThrownBy(() -> Scheme.of(illegalScheme))
          .withMessage(
              "Illegal scheme ["
                  + illegalScheme
                  + "]; Scheme must match [a-zA-Z][a-zA-Z0-9+\\-.]{0,199}")
          .withNoCause()
          .extracting(IllegalScheme::illegalScheme)
          .isEqualTo(String.valueOf(illegalScheme));
    }
  }

  @Nested
  class RegisterMethod {

    @Test
    void registers_custom_scheme_without_default_port() {
      Scheme scheme = Scheme.register("reg1");
      assertThat(scheme.toString()).isEqualTo("reg1");
      assertThat(scheme.defaultPort()).isNull();
    }

    @Test
    void registers_custom_scheme_with_default_port() {
      Scheme scheme = Scheme.register("reg2", Port.of(9999));
      assertThat(scheme.toString()).isEqualTo("reg2");
      assertThat(scheme.defaultPort()).isEqualTo(Port.of(9999));
    }

    @Test
    void registering_same_scheme_twice_returns_same_instance() {
      Scheme scheme1 = Scheme.register("reg3");
      Scheme scheme2 = Scheme.register("reg3");
      assertThat(scheme1).isSameAs(scheme2);
    }

    @Test
    void registering_scheme_with_different_case_returns_canonical_instance() {
      Scheme lowercase = Scheme.register("reg4");
      Scheme uppercase = Scheme.register("REG4");
      assertThat(lowercase).isSameAs(uppercase);
    }

    @Test
    void throws_exception_for_invalid_scheme_pattern() {
      assertThatExceptionOfType(IllegalScheme.class).isThrownBy(() -> Scheme.register("1invalid"));
    }
  }

  @Nested
  class PredefinedSchemes {

    @Test
    void http_scheme_has_port_80() {
      assertThat(Scheme.http.toString()).isEqualTo("http");
      assertThat(Scheme.http.defaultPort()).isEqualTo(Port.of(80));
      assertThat(Scheme.http.isCanonical()).isTrue();
    }

    @Test
    void https_scheme_has_port_443() {
      assertThat(Scheme.https.toString()).isEqualTo("https");
      assertThat(Scheme.https.defaultPort()).isEqualTo(Port.of(443));
      assertThat(Scheme.https.isCanonical()).isTrue();
    }

    @Test
    void ftp_scheme_has_port_21() {
      assertThat(Scheme.ftp.toString()).isEqualTo("ftp");
      assertThat(Scheme.ftp.defaultPort()).isEqualTo(Port.of(21));
      assertThat(Scheme.ftp.isCanonical()).isTrue();
    }

    @Test
    void ssh_scheme_has_port_22() {
      assertThat(Scheme.ssh.toString()).isEqualTo("ssh");
      assertThat(Scheme.ssh.defaultPort()).isEqualTo(Port.of(22));
      assertThat(Scheme.ssh.isCanonical()).isTrue();
    }

    @Test
    void file_scheme_has_no_default_port() {
      assertThat(Scheme.file.toString()).isEqualTo("file");
      assertThat(Scheme.file.defaultPort()).isNull();
      assertThat(Scheme.file.isCanonical()).isTrue();
    }

    @Test
    void mailto_scheme_has_no_default_port() {
      assertThat(Scheme.mailto.toString()).isEqualTo("mailto");
      assertThat(Scheme.mailto.defaultPort()).isNull();
      assertThat(Scheme.mailto.isCanonical()).isTrue();
    }

    @Test
    void parsing_http_returns_predefined_instance() {
      Scheme parsed = Scheme.of("http");
      assertThat(parsed).isSameAs(Scheme.http);
    }

    @Test
    void parsing_https_returns_predefined_instance() {
      Scheme parsed = Scheme.of("https");
      assertThat(parsed).isSameAs(Scheme.https);
    }
  }

  @Nested
  class Canonical {

    @Test
    void lowercase_scheme_is_its_own_canonical() {
      Scheme scheme = Scheme.of("http");
      assertThat(scheme.canonical()).isSameAs(scheme);
      assertThat(scheme.isCanonical()).isTrue();
    }

    @Test
    void uppercase_scheme_has_lowercase_canonical() {
      Scheme uppercase = Scheme.of("HTTP");
      Scheme lowercase = Scheme.of("http");
      assertThat(uppercase.canonical()).isSameAs(lowercase);
      assertThat(uppercase.isCanonical()).isFalse();
    }

    @Test
    void mixed_case_scheme_has_lowercase_canonical() {
      Scheme mixedCase = Scheme.of("HtTp");
      Scheme lowercase = Scheme.of("http");
      assertThat(mixedCase.canonical()).isSameAs(lowercase);
      assertThat(mixedCase.isCanonical()).isFalse();
    }

    @Test
    void custom_lowercase_scheme_is_canonical() {
      Scheme scheme = Scheme.of("canon1");
      assertThat(scheme.canonical()).isSameAs(scheme);
      assertThat(scheme.isCanonical()).isTrue();
    }

    @Test
    void custom_uppercase_scheme_references_lowercase_canonical() {
      Scheme uppercase = Scheme.of("CANON2");
      Scheme lowercase = Scheme.of("canon2");
      assertThat(uppercase.canonical()).isSameAs(lowercase);
      assertThat(uppercase.isCanonical()).isFalse();
    }
  }

  @Nested
  class DefaultPort {

    @Test
    void scheme_without_registered_port_returns_null() {
      Scheme scheme = Scheme.of("port1");
      assertThat(scheme.defaultPort()).isNull();
    }

    @Test
    void registered_scheme_with_port_returns_port() {
      Scheme scheme = Scheme.register("port2", Port.of(8888));
      assertThat(scheme.defaultPort()).isEqualTo(Port.of(8888));
    }

    @Test
    void non_canonical_scheme_inherits_default_port_from_canonical() {
      Scheme canonical = Scheme.register("port3", Port.of(7777));
      Scheme uppercase = Scheme.of("PORT3");
      assertThat(uppercase.defaultPort()).isEqualTo(Port.of(7777));
      assertThat(uppercase.defaultPort()).isSameAs(canonical.defaultPort());
    }

    @Test
    void parsing_uppercase_predefined_scheme_gets_default_port() {
      Scheme uppercase = Scheme.of("HTTP");
      assertThat(uppercase.defaultPort()).isEqualTo(Port.of(80));
    }
  }

  @Nested
  class Equality {

    @Test
    void schemes_with_same_string_are_equal() {
      Scheme scheme1 = Scheme.of("http");
      Scheme scheme2 = Scheme.of("http");
      assertThat(scheme1).isEqualTo(scheme2);
    }

    @Test
    void schemes_with_different_case_are_not_equal() {
      Scheme lowercase = Scheme.of("http");
      Scheme uppercase = Scheme.of("HTTP");
      assertThat(lowercase).isNotEqualTo(uppercase);
    }

    @Test
    void scheme_equals_itself() {
      Scheme scheme = Scheme.of("https");
      assertThat(scheme).isEqualTo(scheme);
    }

    @Test
    void scheme_not_equal_to_null() {
      Scheme scheme = Scheme.of("http");
      assertThat(scheme).isNotEqualTo(null);
    }

    @Test
    void scheme_not_equal_to_string() {
      Scheme scheme = Scheme.of("http");
      assertThat(scheme).isNotEqualTo("http");
    }

    @Test
    void different_schemes_are_not_equal() {
      Scheme http = Scheme.of("http");
      Scheme https = Scheme.of("https");
      assertThat(http).isNotEqualTo(https);
    }

    @Test
    void equals_is_reflexive() {
      Scheme scheme = Scheme.of("ftp");
      assertThat(scheme.equals(scheme)).isTrue();
    }

    @Test
    void equals_is_symmetric() {
      Scheme scheme1 = Scheme.of("ssh");
      Scheme scheme2 = Scheme.of("ssh");
      assertThat(scheme1.equals(scheme2)).isEqualTo(scheme2.equals(scheme1));
    }

    @Test
    void equals_is_transitive() {
      Scheme scheme1 = Scheme.of("mailto");
      Scheme scheme2 = Scheme.of("mailto");
      Scheme scheme3 = Scheme.of("mailto");
      assertThat(scheme1).isEqualTo(scheme2);
      assertThat(scheme2).isEqualTo(scheme3);
      assertThat(scheme1).isEqualTo(scheme3);
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_schemes_have_same_hash_code() {
      Scheme scheme1 = Scheme.of("http");
      Scheme scheme2 = Scheme.of("http");
      assertThat(scheme1.hashCode()).isEqualTo(scheme2.hashCode());
    }

    @Test
    void different_case_schemes_have_different_hash_codes() {
      Scheme lowercase = Scheme.of("http");
      Scheme uppercase = Scheme.of("HTTP");
      assertThat(lowercase.hashCode()).isNotEqualTo(uppercase.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Scheme scheme = Scheme.of("https");
      int hash1 = scheme.hashCode();
      int hash2 = scheme.hashCode();
      assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hash_code_equals_string_hash_code() {
      Scheme scheme = Scheme.of("ftp");
      assertThat(scheme.hashCode()).isEqualTo("ftp".hashCode());
    }
  }

  @Nested
  class ToStringMethod {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "http",
          "https",
          "ftp",
          "ssh",
          "file",
          "mailto",
          "custom",
          "HTTP",
          "HtTpS",
          "tostr1+proto-1.0"
        })
    void to_string_returns_correct_value(String schemeString) {
      Scheme original = Scheme.of(schemeString);
      assertThat(original.toString()).isEqualTo(schemeString);
      String stringForm = original.toString();
      assertThat(stringForm).isEqualTo(schemeString);
    }
  }

  @Nested
  class Caching {

    @Test
    void parsing_same_scheme_twice_returns_same_instance() {
      Scheme scheme1 = Scheme.of("http");
      Scheme scheme2 = Scheme.of("http");
      assertThat(scheme1).isSameAs(scheme2);
    }

    @Test
    void parsing_different_case_returns_different_instances() {
      Scheme lowercase = Scheme.of("http");
      Scheme uppercase = Scheme.of("HTTP");
      assertThat(lowercase).isNotSameAs(uppercase);
    }

    @Test
    void parsing_custom_scheme_twice_returns_same_instance() {
      Scheme scheme1 = Scheme.of("cache1");
      Scheme scheme2 = Scheme.of("cache1");
      assertThat(scheme1).isSameAs(scheme2);
    }

    @Test
    void canonical_and_non_canonical_are_different_instances() {
      Scheme lowercase = Scheme.of("cache2");
      Scheme uppercase = Scheme.of("CACHE2");
      assertThat(lowercase).isNotSameAs(uppercase);
      assertThat(uppercase.canonical()).isSameAs(lowercase);
    }
  }

  @Nested
  class CommonSchemes {

    @Test
    void parses_common_web_schemes() {
      assertThat(Scheme.of("http").toString()).isEqualTo("http");
      assertThat(Scheme.of("https").toString()).isEqualTo("https");
      assertThat(Scheme.of("ws").toString()).isEqualTo("ws");
      assertThat(Scheme.of("wss").toString()).isEqualTo("wss");
    }

    @Test
    void parses_file_transfer_schemes() {
      assertThat(Scheme.of("ftp").toString()).isEqualTo("ftp");
      assertThat(Scheme.of("ftps").toString()).isEqualTo("ftps");
      assertThat(Scheme.of("sftp").toString()).isEqualTo("sftp");
    }

    @Test
    void parses_email_schemes() {
      assertThat(Scheme.of("mailto").toString()).isEqualTo("mailto");
      assertThat(Scheme.of("smtp").toString()).isEqualTo("smtp");
      assertThat(Scheme.of("imap").toString()).isEqualTo("imap");
      assertThat(Scheme.of("pop3").toString()).isEqualTo("pop3");
    }

    @Test
    void parses_database_schemes() {
      assertThat(Scheme.of("jdbc").toString()).isEqualTo("jdbc");
      assertThat(Scheme.of("postgresql").toString()).isEqualTo("postgresql");
      assertThat(Scheme.of("mysql").toString()).isEqualTo("mysql");
      assertThat(Scheme.of("mongodb").toString()).isEqualTo("mongodb");
    }

    @Test
    void parses_git_schemes() {
      assertThat(Scheme.of("git").toString()).isEqualTo("git");
      assertThat(Scheme.of("git+ssh").toString()).isEqualTo("git+ssh");
      assertThat(Scheme.of("git+https").toString()).isEqualTo("git+https");
    }

    @Test
    void parses_data_uri_scheme() {
      assertThat(Scheme.of("data").toString()).isEqualTo("data");
    }

    @Test
    void parses_tel_scheme() {
      assertThat(Scheme.of("tel").toString()).isEqualTo("tel");
    }
  }

  @Nested
  class EdgeCases {

    @Test
    void single_letter_scheme_is_valid() {
      Scheme scheme = Scheme.of("x");
      assertThat(scheme.toString()).isEqualTo("x");
    }

    @Test
    void very_long_scheme_is_valid() {
      String longScheme = "verylongschemenamethatcontainsmanycharactersandisvalidaccordingtotherfc";
      Scheme scheme = Scheme.of(longScheme);
      assertThat(scheme.toString()).isEqualTo(longScheme);
    }

    @Test
    void scheme_with_all_allowed_special_chars() {
      Scheme scheme = Scheme.of("x+proto-v1.0");
      assertThat(scheme.toString()).isEqualTo("x+proto-v1.0");
    }

    @Test
    void scheme_ending_with_digit() {
      Scheme scheme = Scheme.of("http2");
      assertThat(scheme.toString()).isEqualTo("http2");
    }

    @Test
    void scheme_ending_with_plus() {
      Scheme scheme = Scheme.of("svn+");
      assertThat(scheme.toString()).isEqualTo("svn+");
    }

    @Test
    void scheme_ending_with_hyphen() {
      Scheme scheme = Scheme.of("proto-");
      assertThat(scheme.toString()).isEqualTo("proto-");
    }

    @Test
    void scheme_ending_with_dot() {
      Scheme scheme = Scheme.of("custom.");
      assertThat(scheme.toString()).isEqualTo("custom.");
    }

    @Test
    void multiple_consecutive_special_chars() {
      Scheme scheme = Scheme.of("x+-.");
      assertThat(scheme.toString()).isEqualTo("x+-.");
    }
  }

  @Nested
  class IllegalSchemeException {

    @Test
    void exception_contains_invalid_scheme_in_message() {
      assertThatExceptionOfType(IllegalScheme.class)
          .isThrownBy(() -> Scheme.of("1invalid"))
          .withMessageContaining("1invalid");
    }

    @Test
    void exception_for_special_character_prefix() {
      assertThatExceptionOfType(IllegalScheme.class)
          .isThrownBy(() -> Scheme.of("@invalid"))
          .withMessageContaining("@invalid");
    }

    @Test
    void exception_for_whitespace() {
      assertThatExceptionOfType(IllegalScheme.class).isThrownBy(() -> Scheme.of("http scheme"));
    }

    @Test
    void exception_for_colon() {
      assertThatExceptionOfType(IllegalScheme.class).isThrownBy(() -> Scheme.of("http:"));
    }

    @Test
    void exception_for_slash() {
      assertThatExceptionOfType(IllegalScheme.class).isThrownBy(() -> Scheme.of("http/"));
    }
  }
}
