/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.wiremock.url.Lists.concat;
import static org.wiremock.url.whatwg.SnapshotTests.toExpectation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.wiremock.url.AbsoluteUri;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.IllegalUri;
import org.wiremock.url.OpaqueUri;
import org.wiremock.url.Origin;
import org.wiremock.url.Rfc3986Validator;
import org.wiremock.url.ServersideAbsoluteUrl;
import org.wiremock.url.Uri;

public class WhatWGUrlTestManagement {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static final AbsoluteUrl remoteUrl =
      AbsoluteUrl.parse(
          "https://raw.githubusercontent.com/web-platform-tests/wpt/refs/heads/master/url/resources/urltestdata.json");

  private static final String URLTESTDATA_JSON = "urltestdata.json";
  private static final String ADDITIONAL_TESTS_JSON = "additional_tests.json";

  static final List<? extends WhatWGUrlTestCase> remoteData = toWhatWGUrlTestCases(readLocalJson());

  static final List<? extends WhatWGUrlTestCase> additionalTests;

  static {
    try (var additionalTestResource =
        WhatWGUrlTestManagement.class.getResourceAsStream(ADDITIONAL_TESTS_JSON)) {
      additionalTests = toWhatWGUrlTestCases(objectMapper.readTree(additionalTestResource));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static List<WhatWGUrlTestCase> toWhatWGUrlTestCases(JsonNode jsonNode) {
    return jsonNode
        .valueStream()
        .filter(JsonNode::isObject)
        .map(WhatWGUrlTestManagement::mapToWhatWgUrlTestCase)
        .toList();
  }

  public static final List<? extends WhatWGUrlTestCase> testData =
      concat(remoteData, additionalTests);

  private static WhatWGUrlTestCase mapToWhatWgUrlTestCase(JsonNode o) {
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

  private static WireMockSnapshotTestCase mapToWireMockSnapshotTestCase(JsonNode o) {
    try {
      var input = o.get("input").asText();
      var base = o.get("base").textValue();
      var source = mapToWhatWgUrlTestCase(o.get("source"));
      var inputExpectedNode = o.get("inputExpected");
      if (inputExpectedNode != null) {
        var inputExpected =
            objectMapper.treeToValue(inputExpectedNode, UriReferenceExpectation.class);
        var inputNormalised =
            objectMapper.treeToValue(o.get("inputNormalised"), UriReferenceExpectation.class);
        var baseExpected =
            objectMapper.treeToValue(o.get("baseExpected"), UriReferenceExpectation.class);
        var baseNormalised =
            objectMapper.treeToValue(o.get("baseNormalised"), UriReferenceExpectation.class);
        var resolved = objectMapper.treeToValue(o.get("resolved"), UriReferenceExpectation.class);
        var origin = objectMapper.treeToValue(o.get("origin"), UriReferenceExpectation.class);
        var matchesWhatWg = o.get("matchesWhatWg").asBoolean();
        return new SimpleParseSuccess(
            input,
            base,
            inputExpected,
            inputNormalised,
            baseExpected,
            baseNormalised,
            resolved,
            origin,
            source,
            matchesWhatWg);
      } else {
        JsonNode exceptionCauseType = o.get("exceptionCauseType");
        JsonNode exceptionCauseMessage = o.get("exceptionCauseMessage");
        return new SimpleParseFailure(
            input,
            base,
            o.get("exceptionType").textValue(),
            o.get("exceptionMessage").textValue(),
            exceptionCauseType.textValue(),
            exceptionCauseMessage.textValue(),
            source);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static final List<SimpleParseSuccess> whatwg_valid_wiremock_valid =
      (List<SimpleParseSuccess>)
          readResource(
              "whatwg_valid_wiremock_valid",
              WhatWGUrlTestManagement::mapToWireMockSnapshotTestCase);

  @SuppressWarnings("unchecked")
  static List<SimpleParseFailure> whatwg_valid_wiremock_invalid =
      (List<SimpleParseFailure>)
          readResource(
              "whatwg_valid_wiremock_invalid",
              WhatWGUrlTestManagement::mapToWireMockSnapshotTestCase);

  @SuppressWarnings("unchecked")
  static List<SimpleParseSuccess> whatwg_invalid_wiremock_valid =
      (List<SimpleParseSuccess>)
          readResource(
              "whatwg_invalid_wiremock_valid",
              WhatWGUrlTestManagement::mapToWireMockSnapshotTestCase);

  @SuppressWarnings("unchecked")
  static List<SimpleParseFailure> whatwg_invalid_wiremock_invalid =
      (List<SimpleParseFailure>)
          readResource(
              "whatwg_invalid_wiremock_invalid",
              WhatWGUrlTestManagement::mapToWireMockSnapshotTestCase);

  static List<? extends WhatWGUrlTestCase> rfc3986_valid_java_valid =
      readResource("rfc3986_valid_java_valid", WhatWGUrlTestManagement::mapToWhatWgUrlTestCase);

  static List<? extends WhatWGUrlTestCase> rfc3986_valid_java_invalid =
      readResource("rfc3986_valid_java_invalid", WhatWGUrlTestManagement::mapToWhatWgUrlTestCase);

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid_java_valid =
      readResource("rfc3986_invalid_java_valid", WhatWGUrlTestManagement::mapToWhatWgUrlTestCase);

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid_java_invalid =
      readResource("rfc3986_invalid_java_invalid", WhatWGUrlTestManagement::mapToWhatWgUrlTestCase);

  static List<? extends WhatWGUrlTestCase> rfc3986_valid =
      concat(rfc3986_valid_java_valid, rfc3986_valid_java_invalid);

  static List<? extends WhatWGUrlTestCase> rfc3986_invalid =
      concat(rfc3986_invalid_java_valid, rfc3986_invalid_java_invalid);

  static List<? extends WhatWGUrlTestCase> java_valid =
      concat(rfc3986_valid_java_valid, rfc3986_invalid_java_valid);

  static List<? extends WhatWGUrlTestCase> java_invalid =
      concat(rfc3986_valid_java_invalid, rfc3986_invalid_java_invalid);

  public static List<? extends WireMockSnapshotTestCase> whatwg_valid =
      concat(whatwg_valid_wiremock_valid, whatwg_valid_wiremock_invalid);

  static List<? extends WireMockSnapshotTestCase> whatwg_invalid =
      concat(whatwg_invalid_wiremock_valid, whatwg_invalid_wiremock_invalid);

  public static List<? extends SimpleParseSuccess> wiremock_valid =
      concat(whatwg_valid_wiremock_valid, whatwg_invalid_wiremock_valid);

  public static List<? extends SimpleParseFailure> wiremock_invalid =
      concat(whatwg_valid_wiremock_invalid, whatwg_invalid_wiremock_invalid);

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

  static <T> List<? extends T> readResource(String resourceName, Function<JsonNode, T> mapper) {
    try (var resource = WhatWGUrlTestManagement.class.getResourceAsStream(resourceName + ".json")) {
      if (resource == null) {
        return Collections.emptyList();
      } else {
        var json = objectMapper.readTree(resource);
        return json.valueStream().map(mapper).toList();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeResource(String resourceName, List<?> testCases) throws IOException {
    var printer = new OneObjectPerLinePrettyPrinter();
    writeResource(resourceName, testCases, printer);
  }

  public static void writeResource(String resourceName, List<?> testCases, PrettyPrinter printer)
      throws IOException {
    File theFile = getResourceFile(resourceName + ".json");
    if (theFile != null) {
      objectMapper.writer(printer).writeValue(theFile, testCases);
    }
  }

  private static @Nullable File getResourceFile(String resourceName) {
    URL resourceUrl = WhatWGUrlTestManagement.class.getResource(resourceName);
    if (resourceUrl != null && resourceUrl.getProtocol().equals("file")) {
      return new File(
          resourceUrl.getPath().replace("/build/resources/test/", "/src/test/resources/"));
    } else {
      return null;
    }
  }

  static void sortTestData() throws IOException {
    documentJavaUriBehaviour();
    initialiseSimpleSuccesses();
    initialiseSimpleFailures();
    writeResource("whatwg_valid_wiremock_invalid", List.of());
    writeResource("whatwg_invalid_wiremock_valid", List.of(), new CustomDepthPrettyPrinter());
  }

  private static void initialiseSimpleSuccesses() throws IOException {
    List<SimpleParseSuccess> wiremock_valid_whatwg_valid =
        testData.stream()
            .filter(testCase -> testCase instanceof SuccessWhatWGUrlTestCase)
            .map(testCase -> (SuccessWhatWGUrlTestCase) testCase)
            .map(WhatWGUrlTestManagement::toWireMockSuccess)
            .sorted()
            .toList();

    writeResource(
        "whatwg_valid_wiremock_valid", wiremock_valid_whatwg_valid, new CustomDepthPrettyPrinter());
  }

  private static SimpleParseSuccess toWireMockSuccess(SuccessWhatWGUrlTestCase testCase) {

    String input = testCase.input();
    UriReferenceExpectation normalisedInputExpectation;
    UriReferenceExpectation inputExpectation;
    try {
      Uri inputUriRef = Uri.parse(input);
      inputExpectation = toExpectation(inputUriRef);
      normalisedInputExpectation = toExpectation(inputUriRef.normalise());
    } catch (IllegalUri e) {
      inputExpectation = expectation(testCase, input, false);
      normalisedInputExpectation = expectation(testCase, input, true);
    }

    String base = normalise(testCase.base());
    UriReferenceExpectation baseExpectation;
    UriReferenceExpectation baseNormalised;
    if (base != null) {
      try {
        Uri baseUriRef = Uri.parse(base);
        baseExpectation = toExpectation(baseUriRef);
        baseNormalised = toExpectation(baseUriRef.normalise());
      } catch (IllegalUri e) {
        baseExpectation = expectation(testCase, base, false);
        baseNormalised = expectation(testCase, base, true);
      }
    } else {
      baseExpectation = null;
      baseNormalised = null;
    }

    String href = testCase.href();
    UriReferenceExpectation resolved = expectation(testCase, href, true);

    String origin = testCase.origin();
    UriReferenceExpectation originExpectation = null;
    if (normalise(origin) != null) {
      originExpectation =
          new UriReferenceExpectation(
              origin,
              Origin.class.getSimpleName(),
              StringUtils.substringBefore(testCase.protocol(), ":"),
              testCase.host(),
              null,
              null,
              null,
              testCase.hostname().isEmpty() ? null : testCase.hostname(),
              testCase.port() == null ? null : (testCase.port().isEmpty() ? null : testCase.port()),
              "",
              null,
              null);
    }

    return new SimpleParseSuccess(
        input,
        base,
        inputExpectation,
        normalisedInputExpectation,
        baseExpectation,
        baseNormalised,
        resolved,
        originExpectation,
        testCase,
        true);
  }

  private static UriReferenceExpectation expectation(
      SuccessWhatWGUrlTestCase testCase, String input, boolean normalised) {
    String search = substringAfter(testCase.search(), "?");
    String hash = substringAfter(testCase.hash(), "#");
    Class<? extends AbsoluteUri> type = calculateType(testCase, input, normalised);
    return new UriReferenceExpectation(
        (testCase.pathname().isEmpty()
                && normalised
                && testCase.search().isEmpty()
                && testCase.hash().isEmpty())
            ? testCase.href() + "/"
            : testCase.href(),
        type.getSimpleName(),
        substringBefore(testCase.protocol(), ":"),
        authority(testCase, type),
        userInfo(testCase),
        testCase.username().isEmpty() ? null : testCase.username(),
        testCase.password().isEmpty() ? null : testCase.password(),
        AbsoluteUrl.class.isAssignableFrom(type) ? testCase.hostname() : null,
        testCase.port() == null ? null : (testCase.port().isEmpty() ? null : testCase.port()),
        (testCase.pathname().isEmpty() && normalised) ? "/" : testCase.pathname(),
        search.isEmpty() ? null : search,
        hash.isEmpty() ? null : hash);
  }

  private static void initialiseSimpleFailures() throws IOException {
    var wiremock_invalid_whatwg_invalid =
        testData.stream()
            .filter(testCase -> testCase instanceof FailureWhatWGUrlTestCase)
            .map(testCase -> (FailureWhatWGUrlTestCase) testCase)
            .map(WhatWGUrlTestManagement::toWireMockFailure)
            .sorted()
            .toList();

    writeResource("whatwg_invalid_wiremock_invalid", wiremock_invalid_whatwg_invalid);
  }

  @SuppressWarnings("ConstantValue")
  private static void documentJavaUriBehaviour() throws IOException {
    var rfc3986_valid_java_valid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_valid_java_invalid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_invalid_java_valid = new ArrayList<WhatWGUrlTestCase>();
    var rfc3986_invalid_java_invalid = new ArrayList<WhatWGUrlTestCase>();

    testData.forEach(
        test -> {
          var rfc3986_valid = Rfc3986Validator.isValidUriReference(test.input());
          var java_valid = javaValid(test.input());

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
      Uri.parse(test.input());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static Class<? extends AbsoluteUri> calculateType(
      SuccessWhatWGUrlTestCase testCase, String input, boolean normalised) {
    if (!normalised
        && (input.equals(testCase.origin())
            || (!testCase.protocol().isEmpty()
                && testCase.pathname().isEmpty()
                && testCase.username().isEmpty()
                && testCase.password().isEmpty()
                && testCase.search().isEmpty()
                && testCase.hash().isEmpty()))) {
      return Origin.class;
    } else if (testCase.host().isEmpty() && !input.matches("^[a-z]+://($|/.*)")) {
      return OpaqueUri.class;
    } else if (testCase.hash().isEmpty()) {
      return ServersideAbsoluteUrl.class;
    } else {
      return AbsoluteUrl.class;
    }
  }

  private static SimpleParseFailure toWireMockFailure(FailureWhatWGUrlTestCase testCase) {
    String base;
    if (testCase instanceof SimpleFailureWhatWGUrlTestCase simpleFailureWhatWGUrlTestCase) {
      base = normalise(simpleFailureWhatWGUrlTestCase.base());
    } else {
      base = null;
    }
    return new SimpleParseFailure(
        testCase.input(),
        base,
        IllegalUri.class.getSimpleName(),
        "Illegal uri reference: `" + testCase.input() + "`",
        null,
        "",
        testCase);
  }

  private static @Nullable String normalise(@Nullable String input) {
    if (input == null || input.isEmpty() || input.equals("null")) {
      return null;
    }
    return input;
  }

  private static @Nullable String userInfo(SuccessWhatWGUrlTestCase testCase) {
    if (testCase.password().isEmpty()) {
      return testCase.username().isEmpty() ? null : testCase.username();
    } else {
      return testCase.username() + ":" + testCase.password();
    }
  }

  private static @Nullable String authority(
      SuccessWhatWGUrlTestCase testCase, Class<? extends AbsoluteUri> type) {
    var userInfo = userInfo(testCase);
    return userInfo == null ? hostAndPort(testCase, type) : userInfo + "@" + testCase.host();
  }

  private static @Nullable String hostAndPort(
      SuccessWhatWGUrlTestCase testCase, Class<? extends AbsoluteUri> type) {
    return AbsoluteUrl.class.isAssignableFrom(type) ? testCase.host() : null;
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

class CustomDepthPrettyPrinter extends DefaultPrettyPrinter {
  private int depth = 0;
  private final DefaultIndenter normalIndent = new DefaultIndenter("  ", "\n");
  private final DefaultIndenter compactIndent = new DefaultIndenter("", " ");

  public CustomDepthPrettyPrinter() {
    _objectIndenter = normalIndent;
    indentArraysWith(normalIndent);
  }

  @Override
  public void writeStartObject(JsonGenerator g) throws java.io.IOException {
    depth++;
    if (depth > 1) {
      _objectIndenter = compactIndent;
    }
    super.writeStartObject(g);
  }

  @Override
  public void writeEndObject(JsonGenerator g, int nrOfEntries) throws java.io.IOException {
    super.writeEndObject(g, nrOfEntries);
    depth--;
    if (depth <= 1) {
      _objectIndenter = normalIndent;
    }
  }

  @Override
  public DefaultPrettyPrinter createInstance() {
    return new CustomDepthPrettyPrinter();
  }
}
