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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.wiremock.url.Rfc3986Validator;
import org.wiremock.url.UriReference;
import org.wiremock.url.Url;

public class WhatWGUrlTestManagement {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static final Url remoteUrl =
      Url.parse(
          "https://raw.githubusercontent.com/web-platform-tests/wpt/refs/heads/master/url/resources/urltestdata.json");

  private static final String URLTESTDATA_JSON = "urltestdata.json";
  private static final String ADDITIONAL_TESTS_JSON = "additional_tests.json";

  static final List<? extends WhatWGUrlTestCase> remoteData =
      readLocalJson()
          .valueStream()
          .filter(JsonNode::isObject)
          .map(WhatWGUrlTestManagement::map)
          .toList();

  static final List<? extends WhatWGUrlTestCase> additionalTests;

  static {
    try (var additionalTestResource =
        WhatWGUrlTestManagement.class.getResourceAsStream(ADDITIONAL_TESTS_JSON)) {
      additionalTests =
          objectMapper.readValue(
              additionalTestResource, new TypeReference<List<SuccessWhatWGUrlTestCase>>() {});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static final List<? extends WhatWGUrlTestCase> testData = concat(remoteData, additionalTests);

  private static WhatWGUrlTestCase map(JsonNode o) {
    try {
      JsonNode failure = o.get("failure");
      if (failure != null && failure.asBoolean()) {
        if (o.get("relativeTo") == null) {
          return objectMapper.treeToValue(o, SimpleFailureWhatWGUrlTestCase.class);
        } else {
          return objectMapper.treeToValue(o, RelativeToFailureWhatWGUrlTestCase.class);
        }
      } else {
        return objectMapper.treeToValue(o, SuccessWhatWGUrlTestCase.class);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static final List<SuccessWhatWGUrlTestCase> whatwg_valid_rfc3986_valid_wiremock_valid =
      (List<SuccessWhatWGUrlTestCase>) readResource("whatwg_valid_rfc3986_valid_wiremock_valid");

  @SuppressWarnings("unchecked")
  static List<SuccessWhatWGUrlTestCase> whatwg_valid_rfc3986_valid_wiremock_invalid =
      (List<SuccessWhatWGUrlTestCase>) readResource("whatwg_valid_rfc3986_valid_wiremock_invalid");

  @SuppressWarnings("unchecked")
  static List<SuccessWhatWGUrlTestCase> whatwg_valid_rfc3986_invalid_wiremock_valid =
      (List<SuccessWhatWGUrlTestCase>) readResource("whatwg_valid_rfc3986_invalid_wiremock_valid");

  @SuppressWarnings("unchecked")
  static List<SuccessWhatWGUrlTestCase> whatwg_valid_rfc3986_invalid_wiremock_invalid =
      (List<SuccessWhatWGUrlTestCase>)
          readResource("whatwg_valid_rfc3986_invalid_wiremock_invalid");

  @SuppressWarnings("unchecked")
  static List<? extends FailureWhatWGUrlTestCase> whatwg_invalid_rfc3986_valid_wiremock_valid =
      (List<? extends FailureWhatWGUrlTestCase>)
          readResource("whatwg_invalid_rfc3986_valid_wiremock_valid");

  @SuppressWarnings("unchecked")
  static List<? extends FailureWhatWGUrlTestCase> whatwg_invalid_rfc3986_valid_wiremock_invalid =
      (List<? extends FailureWhatWGUrlTestCase>)
          readResource("whatwg_invalid_rfc3986_valid_wiremock_invalid");

  @SuppressWarnings("unchecked")
  static List<? extends FailureWhatWGUrlTestCase> whatwg_invalid_rfc3986_invalid_wiremock_valid =
      (List<? extends FailureWhatWGUrlTestCase>)
          readResource("whatwg_invalid_rfc3986_invalid_wiremock_valid");

  @SuppressWarnings("unchecked")
  static List<? extends FailureWhatWGUrlTestCase> whatwg_invalid_rfc3986_invalid_wiremock_invalid =
      (List<? extends FailureWhatWGUrlTestCase>)
          readResource("whatwg_invalid_rfc3986_invalid_wiremock_invalid");

  static List<? extends WhatWGUrlTestCase> rfc3986_valid_java_valid =
      readResource("rfc3986_valid_java_valid");

  static List<? extends WhatWGUrlTestCase> rfc3986_valid_java_invalid =
      readResource("rfc3986_valid_java_invalid");

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid_java_valid =
      readResource("rfc3986_invalid_java_valid");

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid_java_invalid =
      readResource("rfc3986_invalid_java_invalid");

  static List<? extends WhatWGUrlTestCase> java_valid =
      concat(rfc3986_valid_java_valid, rfc3986_invalid_java_valid);

  static List<? extends WhatWGUrlTestCase> java_invalid =
      concat(rfc3986_valid_java_invalid, rfc3986_invalid_java_invalid);

  public static List<? extends SuccessWhatWGUrlTestCase> whatwg_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid);

  static List<? extends FailureWhatWGUrlTestCase> whatwg_invalid =
      concat(
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  static List<? extends WhatWGUrlTestCase> rfc3986_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid);

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid =
      concat(
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  public static List<? extends SuccessWhatWGUrlTestCase> whatwg_valid_wiremock_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid, whatwg_valid_rfc3986_invalid_wiremock_valid);

  public static List<? extends WhatWGUrlTestCase> wiremock_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid);

  public static List<? extends WhatWGUrlTestCase> wiremock_invalid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  private static JsonNode readLocalJson() {
    try {
      return objectMapper.readTree(readLocal());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  static void updateTestData(String testData) throws IOException {
    File theFile = getResourceFile(URLTESTDATA_JSON);
    if (theFile != null) {
      try (var writer = new BufferedWriter(new FileWriter(theFile))) {
        writer.write(testData);
        System.err.println(
            "Updated with latest from " + remoteUrl + ", test should pass next time");
      }
    }
  }

  static String readLocal() {
    try (var testData = WhatWGUrlTestManagement.class.getResourceAsStream(URLTESTDATA_JSON)) {
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

  static List<? extends WhatWGUrlTestCase> readResource(String resourceName) {
    try (var resource = WhatWGUrlTestManagement.class.getResourceAsStream(resourceName + ".json")) {
      if (resource == null) {
        return Collections.emptyList();
      } else {
        var json = objectMapper.readTree(resource);
        return json.valueStream().map(WhatWGUrlTestManagement::map).toList();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void writeResource(String resourceName, List<WhatWGUrlTestCase> testCases)
      throws IOException {
    File theFile = getResourceFile(resourceName + ".json");
    if (theFile != null) {
      objectMapper.writer(new OneObjectPerLinePrettyPrinter()).writeValue(theFile, testCases);
    }
  }

  private static File getResourceFile(String resourceName) {
    URL resourceUrl = WhatWGUrlTestManagement.class.getResource(resourceName);
    if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
      return new File(
          resourceUrl.getPath().replace("/build/resources/test/", "/src/test/resources/"));
    } else {
      return null;
    }
  }

  static <C extends Collection<T>, T> List<? extends T> concat(Collection<? extends C> lists) {
    return concat(lists.stream());
  }

  @SafeVarargs
  static <T> List<? extends T> concat(Collection<? extends T>... lists) {
    return concat(Stream.of(lists));
  }

  static <C extends Collection<? extends T>, T> List<? extends T> concat(Stream<C> lists) {
    return lists.flatMap(Collection::stream).toList();
  }

  @SuppressWarnings("ConstantValue")
  static void sortTestData() throws IOException {
    var whatwg_valid_rfc3986_valid_wiremock_valid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_valid_rfc3986_valid_wiremock_invalid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_valid_rfc3986_invalid_wiremock_valid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_valid_rfc3986_invalid_wiremock_invalid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_invalid_rfc3986_valid_wiremock_valid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_invalid_rfc3986_valid_wiremock_invalid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_invalid_rfc3986_invalid_wiremock_valid = new ArrayList<WhatWGUrlTestCase>();
    var whatwg_invalid_rfc3986_invalid_wiremock_invalid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_valid_java_valid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_valid_java_invalid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_invalid_java_valid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_invalid_java_invalid = new ArrayList<WhatWGUrlTestCase>();

    testData.forEach(
        test -> {
          var whatwg_valid = !test.failure();
          var rfc3986_valid = Rfc3986Validator.isValidUriReference(test.input());

          var wiremock_valid = shouldBeValid(rfc3986_valid, test);
          var java_valid = javaValid(test.input());

          if (whatwg_valid && rfc3986_valid && wiremock_valid) {
            whatwg_valid_rfc3986_valid_wiremock_valid.add(test);
          } else if (whatwg_valid && rfc3986_valid && !wiremock_valid) {
            whatwg_valid_rfc3986_valid_wiremock_invalid.add(test);
          } else if (whatwg_valid && !rfc3986_valid && wiremock_valid) {
            whatwg_valid_rfc3986_invalid_wiremock_valid.add(test);
          } else if (whatwg_valid && !rfc3986_valid && !wiremock_valid) {
            whatwg_valid_rfc3986_invalid_wiremock_invalid.add(test);
          } else if (!whatwg_valid && rfc3986_valid && wiremock_valid) {
            whatwg_invalid_rfc3986_valid_wiremock_valid.add(test);
          } else if (!whatwg_valid && rfc3986_valid && !wiremock_valid) {
            whatwg_invalid_rfc3986_valid_wiremock_invalid.add(test);
          } else if (!whatwg_valid && !rfc3986_valid && wiremock_valid) {
            whatwg_invalid_rfc3986_invalid_wiremock_valid.add(test);
          } else if (!whatwg_valid && !rfc3986_valid && !wiremock_valid) {
            whatwg_invalid_rfc3986_invalid_wiremock_invalid.add(test);
          } else {
            throw new IllegalStateException("Unreachable");
          }

          if (rfc3986_valid && java_valid) {
            rfc3986_valid_java_valid.add(test);
          } else if (rfc3986_valid && !java_valid) {
            rfc3986_valid_java_invalid.add(test);
          } else if (!rfc3986_valid && java_valid) {
            rfc3986_invalid_java_valid.add(test);
          } else if (!rfc3986_valid && !java_valid) {
            rfc3986_invalid_java_invalid.add(test);
          } else {
            throw new IllegalStateException("Unreachable");
          }
        });

    writeResource(
        "whatwg_valid_rfc3986_valid_wiremock_valid", whatwg_valid_rfc3986_valid_wiremock_valid);
    writeResource(
        "whatwg_valid_rfc3986_valid_wiremock_invalid", whatwg_valid_rfc3986_valid_wiremock_invalid);
    writeResource(
        "whatwg_valid_rfc3986_invalid_wiremock_valid", whatwg_valid_rfc3986_invalid_wiremock_valid);
    writeResource(
        "whatwg_valid_rfc3986_invalid_wiremock_invalid",
        whatwg_valid_rfc3986_invalid_wiremock_invalid);
    writeResource(
        "whatwg_invalid_rfc3986_valid_wiremock_valid", whatwg_invalid_rfc3986_valid_wiremock_valid);
    writeResource(
        "whatwg_invalid_rfc3986_valid_wiremock_invalid",
        whatwg_invalid_rfc3986_valid_wiremock_invalid);
    writeResource(
        "whatwg_invalid_rfc3986_invalid_wiremock_valid",
        whatwg_invalid_rfc3986_invalid_wiremock_valid);
    writeResource(
        "whatwg_invalid_rfc3986_invalid_wiremock_invalid",
        whatwg_invalid_rfc3986_invalid_wiremock_invalid);
    writeResource("rfc3986_valid_java_valid", rfc3986_valid_java_valid);
    writeResource("rfc3986_valid_java_invalid", rfc3986_valid_java_invalid);
    writeResource("rfc3986_invalid_java_valid", rfc3986_invalid_java_valid);
    writeResource("rfc3986_invalid_java_invalid", rfc3986_invalid_java_invalid);
  }

  private static boolean javaValid(String input) {
    try {
      new URI(input);
      return true;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  @SuppressWarnings("unused")
  private static boolean shouldBeValid(boolean rfc3986Valid, WhatWGUrlTestCase test) {
    try {
      UriReference.parse(test.input());
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

class OneObjectPerLinePrettyPrinter extends DefaultPrettyPrinter {

  public OneObjectPerLinePrettyPrinter() {
    super();
    _arrayIndenter = new DefaultIndenter("", "");
    _objectIndenter = new DefaultIndenter("", "");
    _spacesInObjectEntries = true;
  }

  @Override
  public DefaultPrettyPrinter createInstance() {
    return new OneObjectPerLinePrettyPrinter();
  }

  @Override
  public void writeStartArray(JsonGenerator g) throws IOException {
    g.writeRaw('[');
  }

  @Override
  public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
    if (nrOfValues > 0) {
      g.writeRaw('\n');
    }
    g.writeRaw(']');
  }

  @Override
  public void beforeArrayValues(JsonGenerator g) throws IOException {
    g.writeRaw('\n');
  }

  @Override
  public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
    g.writeRaw(',');
    g.writeRaw('\n');
  }

  @Override
  public void writeStartObject(JsonGenerator g) throws IOException {
    g.writeRaw("{ ");
  }

  @Override
  public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
    g.writeRaw(" }");
  }

  @Override
  public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
    g.writeRaw(": ");
  }

  @Override
  public void writeObjectEntrySeparator(JsonGenerator g) throws IOException {
    g.writeRaw(", ");
  }
}
