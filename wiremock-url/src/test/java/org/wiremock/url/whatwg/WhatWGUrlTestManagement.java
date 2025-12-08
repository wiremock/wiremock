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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.wiremock.url.IllegalUrlReference;
import org.wiremock.url.Rfc3986Validator;
import org.wiremock.url.Url;
import org.wiremock.url.UrlReference;

class WhatWGUrlTestManagement {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static final Url remoteUrl =
      Url.parse(
          "https://raw.githubusercontent.com/web-platform-tests/wpt/refs/heads/master/url/resources/urltestdata.json");

  private static final String URLTESTDATA_JSON = "urltestdata.json";

  static final List<WhatWGUrlTestCase> testData =
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

  private static JsonNode readLocalJson() {
    try {
      return objectMapper.readTree(readLocal());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  static void updateTestData(String testData) throws URISyntaxException, IOException {
    URL theFile = WhatWGUrlTests.class.getResource(URLTESTDATA_JSON);
    if (theFile != null && theFile.getProtocol().equals("file")) {
      File file = new File(theFile.toURI());
      try (var writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(testData);
        System.err.println(
            "Updated with latest from " + remoteUrl + ", test should pass next time");
      }
    }
  }

  static String readLocal() {
    try (var testData = WhatWGUrlTests.class.getResourceAsStream(URLTESTDATA_JSON)) {
      assert testData != null;
      return normaliseToString(testData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static String readRemote() throws IOException {
    try (var testData = new URL(remoteUrl.toString()).openStream()) {
      return normaliseToString(testData);
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

  public static void main(String[] args) throws IOException {
    var whatwg_valid_rfc3986_valid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_valid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_invalid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_valid_rfc3986_invalid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_valid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_valid_wiremock_invalid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_invalid_wiremock_valid = new LinkedHashSet<String>();
    var whatwg_invalid_rfc3986_invalid_wiremock_invalid = new LinkedHashSet<String>();

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
    //noinspection ResultOfMethodCallIgnored
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

  private static boolean rfc3986_valid(String input) {
    return Rfc3986Validator.isValidUriReference(input);
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
