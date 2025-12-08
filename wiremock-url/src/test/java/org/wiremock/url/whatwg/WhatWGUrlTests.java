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
package org.wiremock.url.whatwg;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.IllegalUrlReference;
import org.wiremock.url.Rfc3986Validator;
import org.wiremock.url.Url;
import org.wiremock.url.UrlReference;

class WhatWGUrlTests {

  private static final Url remoteUrl =
      Url.parse(
          "https://raw.githubusercontent.com/web-platform-tests/wpt/refs/heads/master/url/resources/urltestdata.json");

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @ParameterizedTest
  @FieldSource("whatwg_valid")
  void we_accept_all_whatwg_valid_urls(WhatWGUrlTestCase testCase) {
    var url = UrlReference.parse(testCase.input());
  }

  @ParameterizedTest
  @FieldSource("whatwg_valid")
  void we_reject_all_whatwg_invalid_urls(WhatWGUrlTestCase testCase) {
    assertThatExceptionOfType(IllegalUrlReference.class)
        .isThrownBy(() -> UrlReference.parse(testCase.input()));
  }

  @Test
  @EnabledIf("remoteDataReachable")
  void test_data_is_up_to_date() throws IOException, URISyntaxException {
    String expected = readRemote();
    try {
      assertThat(readLocal()).isEqualTo(expected);
    } catch (AssertionError e) {
      URL theFile = WhatWGUrlTests.class.getResource("urltestdata.json");
      if (theFile != null && theFile.getProtocol().equals("file")) {
        File file = new File(theFile.toURI());
        try (var writer = new BufferedWriter(new FileWriter(file))) {
          writer.write(expected);
          System.err.println(
              "Updated with latest from " + remoteUrl + ", test should pass next time");
        }
      }
      throw e;
    }
  }

  private static final List<WhatWGUrlTestCase> testData =
      readLocalJson()
          .valueStream()
          .filter(JsonNode::isObject)
          .map(
              o -> {
                try {
                  return objectMapper.treeToValue(o, WhatWGUrlTestCase.class);
                } catch (JsonProcessingException e) {
                  throw new AssertionError(e);
                }
              })
          .toList();

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> whatwg_valid =
      testData.stream().filter(test -> !test.failure()).toList();

  @SuppressWarnings("unused")
  private static final List<WhatWGUrlTestCase> whatwg_invalid =
      testData.stream().filter(WhatWGUrlTestCase::failure).toList();

  private static String readLocal() {
    try (var testData = WhatWGUrlTests.class.getResourceAsStream("urltestdata.json")) {
      assert testData != null;
      return normaliseToString(testData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String normaliseToString(InputStream testData) throws IOException {
    String value = new String(testData.readAllBytes(), UTF_8);
    if (value.endsWith("\n")) {
      return value;
    } else {
      return value + "\n";
    }
  }

  private static JsonNode readLocalJson() {
    try {
      return objectMapper.readTree(readLocal());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String readRemote() throws IOException {
    try (var testData = new URL(remoteUrl.toString()).openStream()) {
      return normaliseToString(testData);
    }
  }

  private static boolean remoteDataReachable() {
    return hostReachable(remoteUrl);
  }

  @SuppressWarnings("SameParameterValue")
  private static boolean hostReachable(Url url) {
    try (Socket socket = new Socket()) {
      //noinspection DataFlowIssue
      socket.connect(new InetSocketAddress(url.host().toString(), url.resolvedPort().port()), 500);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static void main(String[] args) throws IOException {
    var whatwg_valid_rfc3986_valid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_valid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_invalid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_invalid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_valid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_valid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_invalid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_invalid_wiremock_invalid = new LinkedHashSet<String>();

    //    System.out.println("rfc3986_valid, java not valid");
    //    testData.forEach(test -> {
    //      var rfc3986_valid = rfc3986_valid(test.input());
    //      var java_uri_valid = java_uri_valid(test.input());
    //      if (rfc3986_valid && java_uri_valid instanceof String) {
    //        System.out.println("`"+test.input() + "`   " + java_uri_valid);
    //      }
    //    });
    //    System.out.println();
    //    System.out.println("rfc3986 not valid, java valid");
    //    testData.forEach(test -> {
    //      var rfc3986_valid = rfc3986_valid(test.input());
    //      var java_uri_valid = java_uri_valid(test.input());
    //      if (!rfc3986_valid && java_uri_valid instanceof URI java_uri) {
    //        System.out.println("`"+test.input() + "` Java = "
    //            + "scheme: `"+java_uri.getScheme() + "` "
    //            + "authority: `"+java_uri.getAuthority() + "` "
    //            + "userInfo: `"+java_uri.getUserInfo() + "` "
    //            + "host: `"+java_uri.getHost() + "` "
    //            + "port: `"+java_uri.getPort() + "` "
    //            + "path: `"+java_uri.getPath() + "` "
    //            + "query: `"+java_uri.getQuery() + "` "
    //            + "fragment: `"+java_uri.getFragment() + "` "
    //            + "schemeSpecificPart: `"+java_uri.getSchemeSpecificPart() + "` "
    //        );
    //      }
    //    });

    testData.forEach(
        test -> {
          var whatwg_valid = !test.failure();
          var rfc3986_valid = rfc3986_valid(test.input());
          var wiremock_valid = wiremock_valid(test.input());
          if (whatwg_valid && rfc3986_valid && wiremock_valid) {
            whatwg_valid_rfc3986_valid_wiremock_valid.add(test.input());
          }
          if (whatwg_valid && rfc3986_valid && !wiremock_valid) {
            whatwg_valid_rfc3986_valid_wiremock_invalid.add(test.input());
          }
          if (whatwg_valid && !rfc3986_valid && wiremock_valid) {
            whatwg_valid_rfc3986_invalid_wiremock_valid.add(test.input());
          }
          if (whatwg_valid && !rfc3986_valid && !wiremock_valid) {
            whatwg_valid_rfc3986_invalid_wiremock_invalid.add(test.input());
          }
          if (!whatwg_valid && rfc3986_valid && wiremock_valid) {
            whatwg_invalid_rfc3986_valid_wiremock_valid.add(test.input());
          }
          if (!whatwg_valid && rfc3986_valid && !wiremock_valid) {
            whatwg_invalid_rfc3986_valid_wiremock_invalid.add(test.input());
          }
          if (!whatwg_valid && !rfc3986_valid && wiremock_valid) {
            whatwg_invalid_rfc3986_invalid_wiremock_valid.add(test.input());
          }
          if (!whatwg_valid && !rfc3986_valid && !wiremock_valid) {
            whatwg_invalid_rfc3986_invalid_wiremock_invalid.add(test.input());
          }
        });

    report("whatwg_valid_rfc3986_valid_wiremock_valid", whatwg_valid_rfc3986_valid_wiremock_valid);

    report(
        "whatwg_valid_rfc3986_valid_wiremock_invalid", whatwg_valid_rfc3986_valid_wiremock_invalid);

    report(
        "whatwg_valid_rfc3986_invalid_wiremock_valid", whatwg_valid_rfc3986_invalid_wiremock_valid);

    report(
        "whatwg_valid_rfc3986_invalid_wiremock_invalid",
        whatwg_valid_rfc3986_invalid_wiremock_invalid);

    report(
        "whatwg_invalid_rfc3986_valid_wiremock_valid", whatwg_invalid_rfc3986_valid_wiremock_valid);

    report(
        "whatwg_invalid_rfc3986_valid_wiremock_invalid",
        whatwg_invalid_rfc3986_valid_wiremock_invalid);

    report(
        "whatwg_invalid_rfc3986_invalid_wiremock_valid",
        whatwg_invalid_rfc3986_invalid_wiremock_valid);

    report(
        "whatwg_invalid_rfc3986_invalid_wiremock_invalid",
        whatwg_invalid_rfc3986_invalid_wiremock_invalid);
  }

  private static void report(String title, Collection<String> items) throws IOException {
    File file = new File("tmp/" + title + ".json");
    file.createNewFile();
    try (var writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("[\n");
      List<String> escaped =
          items.stream()
              .map(
                  s -> {
                    try {
                      return objectMapper.writeValueAsString(s);
                    } catch (JsonProcessingException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .toList();
      writer.write(String.join(",\n", escaped));
      writer.write("\n]\n");
    }
  }

  private static void printEscaped(Object s) {
    try {
      objectMapper.writeValueAsString(s);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Pattern rfc3986Pattern =
      Pattern.compile(
          "^[A-Za-z][A-Za-z0-9+.-]*:(//(([A-Za-z0-9._~!$&'()*+,;=:-]|%[0-9A-Fa-f]{2})*@)?(\\[([0-9A-Fa-f:.]+|v[0-9A-Fa-f]+\\.[A-Za-z0-9._~!$&'()*+,;=:-]+)]|([0-9]{1,3}\\.){3}[0-9]{1,3}|([A-Za-z0-9._~!$&'()*+,;=-]|%[0-9A-Fa-f]{2})*)(:[0-9]*)?(/([A-Za-z0-9._~!$&'()*+,;=:@/-]|%[0-9A-Fa-f]{2})*)?|(/?([A-Za-z0-9._~!$&'()*+,;=:@/-]|%[0-9A-Fa-f]{2})*))(\\?([A-Za-z0-9._~!$&'()*+,;=:@/?-]|%[0-9A-Fa-f]{2})*)?(#([A-Za-z0-9._~!$&'()*+,;=:@/?-]|%[0-9A-Fa-f]{2})*)?$");

  private static boolean rfc3986_valid(String input) {
    return Rfc3986Validator.isValidUriReference(input);
  }

  private static Object java_uri_valid(String input) {
    try {
      return new URI(input);
    } catch (URISyntaxException e) {
      return e.getMessage();
    }
  }

  private static boolean wiremock_valid(String input) {
    try {
      UrlReference.parse(input);
      return true;
    } catch (IllegalUrlReference ignored) {
      return false;
    }
  }
}
