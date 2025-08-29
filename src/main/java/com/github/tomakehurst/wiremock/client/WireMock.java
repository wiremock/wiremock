/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.common.ContentTypes.LOCATION;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.thatMatch;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;

import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.DelayDistribution;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.AbsentPattern;
import com.github.tomakehurst.wiremock.matching.AfterDateTimePattern;
import com.github.tomakehurst.wiremock.matching.BeforeDateTimePattern;
import com.github.tomakehurst.wiremock.matching.BinaryEqualToPattern;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.EqualToDateTimePattern;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.EqualToXmlPattern;
import com.github.tomakehurst.wiremock.matching.ExactMatchMultiValuePattern;
import com.github.tomakehurst.wiremock.matching.IncludesMatchMultiValuePattern;
import com.github.tomakehurst.wiremock.matching.LogicalAnd;
import com.github.tomakehurst.wiremock.matching.LogicalOr;
import com.github.tomakehurst.wiremock.matching.MatchesJsonPathPattern;
import com.github.tomakehurst.wiremock.matching.MatchesJsonSchemaPattern;
import com.github.tomakehurst.wiremock.matching.MatchesXPathPattern;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import com.github.tomakehurst.wiremock.matching.NegativeContainsPattern;
import com.github.tomakehurst.wiremock.matching.NegativeRegexPattern;
import com.github.tomakehurst.wiremock.matching.NotPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathTemplatePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.standalone.RemoteMappingsLoader;
import com.github.tomakehurst.wiremock.store.InMemorySettingsStore;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubImportBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import com.github.tomakehurst.wiremock.verification.diff.Diff;
import com.networknt.schema.SpecVersion;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A builder for creating and configuring {@link WireMock} client instances.
 *
 * <p>This provides a fluent API for specifying the connection details of a WireMock server,
 * including host, port, scheme, proxy settings, and authentication.
 *
 * @see WireMock
 */
public class WireMock {

  private static final int DEFAULT_PORT = 8080;
  private static final String DEFAULT_HOST = "localhost";

  private final Admin admin;

  private final SettingsStore settingsStore = new InMemorySettingsStore();

  private static final InheritableThreadLocal<WireMock> defaultInstance =
      new InheritableThreadLocal<>() {
        @Override
        protected WireMock initialValue() {
          return WireMock.create().build();
        }
      };

  /**
   * Creates a new {@link WireMockBuilder} for configuring a client instance.
   *
   * @return a new builder instance.
   */
  public static WireMockBuilder create() {
    return new WireMockBuilder();
  }

  /**
   * Constructs a new WireMock client with a pre-configured Admin interface.
   *
   * @param admin The admin interface for communicating with the server.
   */
  public WireMock(Admin admin) {
    this.admin = admin;
  }

  /**
   * Constructs a new WireMock client configured for localhost on the specified port.
   *
   * @param port The port of the WireMock server.
   */
  public WireMock(int port) {
    this(DEFAULT_HOST, port);
  }

  /**
   * Constructs a new WireMock client.
   *
   * @param host The hostname or IP address of the server.
   * @param port The port of the server.
   */
  public WireMock(String host, int port) {
    admin = new HttpAdminClient(host, port);
  }

  /**
   * Constructs a new WireMock client.
   *
   * @param host The hostname or IP address of the server.
   * @param port The port of the server.
   * @param urlPathPrefix The prefix for all admin URLs.
   */
  public WireMock(String host, int port, String urlPathPrefix) {
    admin = new HttpAdminClient(host, port, urlPathPrefix);
  }

  /**
   * Creates a new WireMock client instance with the given scheme, host and port.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   */
  public WireMock(String scheme, String host, int port) {
    admin = new HttpAdminClient(scheme, host, port);
  }

  /**
   * Creates a new WireMock client instance with the given scheme and host, using the default port
   * depending on the scheme.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   */
  public WireMock(String scheme, String host) {
    admin = new HttpAdminClient(scheme, host);
  }

  /**
   * Creates a new WireMock client instance with the given scheme, host, port, and a URL path prefix
   * for all admin API requests.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   * @param urlPathPrefix the path prefix to prepend to all admin API endpoints
   */
  public WireMock(String scheme, String host, int port, String urlPathPrefix) {
    admin = new HttpAdminClient(scheme, host, port, urlPathPrefix);
  }

  /**
   * Creates a new WireMock client instance with advanced configuration options, including proxy
   * settings and authentication.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   * @param urlPathPrefix the path prefix to prepend to all admin API endpoints
   * @param hostHeader the value to use for the Host HTTP header
   * @param proxyHost the hostname of the proxy server to route requests through
   * @param proxyPort the port number of the proxy server
   * @param authenticator an optional {@link ClientAuthenticator} to use for authentication
   */
  public WireMock(
      String scheme,
      String host,
      int port,
      String urlPathPrefix,
      String hostHeader,
      String proxyHost,
      int proxyPort,
      ClientAuthenticator authenticator) {
    admin =
        new HttpAdminClient(
            scheme, host, port, urlPathPrefix, hostHeader, proxyHost, proxyPort, authenticator);
  }

  /** Constructs a new WireMock client with default settings (localhost:8080). */
  public WireMock() {
    admin = new HttpAdminClient(DEFAULT_HOST, DEFAULT_PORT);
  }

  /**
   * Starts defining a new stub mapping.
   *
   * @param mappingBuilder The builder for the stub mapping.
   * @return The created {@link StubMapping}.
   */
  public static StubMapping givenThat(MappingBuilder mappingBuilder) {
    return defaultInstance.get().register(mappingBuilder);
  }

  /**
   * An alias for {@link #givenThat(MappingBuilder)}.
   *
   * @param mappingBuilder The builder for the stub mapping.
   * @return The created {@link StubMapping}.
   */
  public static StubMapping stubFor(MappingBuilder mappingBuilder) {
    return givenThat(mappingBuilder);
  }

  /**
   * Edits an existing stub mapping.
   *
   * @param mappingBuilder The builder for the stub mapping to be edited.
   */
  public static void editStub(MappingBuilder mappingBuilder) {
    defaultInstance.get().editStubMapping(mappingBuilder);
  }

  /**
   * Removes a stub mapping.
   *
   * @param mappingBuilder The stub mapping to remove.
   */
  public static void removeStub(MappingBuilder mappingBuilder) {
    defaultInstance.get().removeStubMapping(mappingBuilder);
  }

  /**
   * Removes a stub mapping.
   *
   * @param stubMapping The stub mapping to remove.
   */
  public static void removeStub(StubMapping stubMapping) {
    defaultInstance.get().removeStubMapping(stubMapping);
  }

  /**
   * Removes a stub mapping by its ID.
   *
   * @param id The UUID of the stub mapping to remove.
   */
  public static void removeStub(UUID id) {
    defaultInstance.get().removeStubMapping(id);
  }

  /**
   * Lists all registered stub mappings.
   *
   * @return A {@link ListStubMappingsResult} containing all stubs.
   */
  public static ListStubMappingsResult listAllStubMappings() {
    return defaultInstance.get().allStubMappings();
  }

  /**
   * Retrieves a single stub mapping by its ID.
   *
   * @param id The UUID of the stub mapping.
   * @return The requested {@link StubMapping}.
   */
  public static StubMapping getSingleStubMapping(UUID id) {
    return defaultInstance.get().getStubMapping(id).getItem();
  }

  /**
   * Configures the static client to connect to localhost on the specified port.
   *
   * @param port The port of the WireMock server.
   */
  public static void configureFor(int port) {
    defaultInstance.set(WireMock.create().port(port).build());
  }

  /**
   * Configures the static client to connect to the specified host and port.
   *
   * @param host The hostname of the WireMock server.
   * @param port The port of the WireMock server.
   */
  public static void configureFor(String host, int port) {
    defaultInstance.set(WireMock.create().host(host).port(port).build());
  }

  /**
   * Configures the default static WireMock client instance with the given host, port, and URL path
   * prefix.
   *
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   * @param urlPathPrefix the path prefix to prepend to all admin API endpoints
   */
  public static void configureFor(String host, int port, String urlPathPrefix) {
    defaultInstance.set(
        WireMock.create().host(host).port(port).urlPathPrefix(urlPathPrefix).build());
  }

  /**
   * Configures the default static WireMock client instance with the given scheme, host, port, and
   * URL path prefix.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   * @param urlPathPrefix the path prefix to prepend to all admin API endpoints
   */
  public static void configureFor(String scheme, String host, int port, String urlPathPrefix) {
    defaultInstance.set(
        WireMock.create()
            .scheme(scheme)
            .host(host)
            .port(port)
            .urlPathPrefix(urlPathPrefix)
            .build());
  }

  /**
   * Configures the default static WireMock client instance with the given scheme, host and port.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   */
  public static void configureFor(String scheme, String host, int port) {
    defaultInstance.set(WireMock.create().scheme(scheme).host(host).port(port).build());
  }

  /**
   * Configures the default static WireMock client instance with the given scheme, host, port, and
   * proxy configuration.
   *
   * @param scheme the protocol scheme to use (e.g. "http" or "https")
   * @param host the hostname or IP address of the WireMock server
   * @param port the port number where the WireMock server is running
   * @param proxyHost the hostname of the proxy server to route requests through
   * @param proxyPort the port number of the proxy server
   */
  public static void configureFor(
      String scheme, String host, int port, String proxyHost, int proxyPort) {
    defaultInstance.set(
        WireMock.create()
            .scheme(scheme)
            .host(host)
            .port(port)
            .urlPathPrefix("")
            .hostHeader(null)
            .proxyHost(proxyHost)
            .proxyPort(proxyPort)
            .build());
  }

  /**
   * Configures the default static WireMock client instance with the provided {@link WireMock}
   * client.
   *
   * @param client an existing WireMock client instance to set as the default
   */
  public static void configureFor(WireMock client) {
    defaultInstance.set(client);
  }

  /** Resets the static client to its default configuration (localhost:8080). */
  public static void configure() {
    defaultInstance.set(WireMock.create().build());
  }

  /**
   * Creates a {@link StringValuePattern} that matches values equal to the given string.
   *
   * @param value the expected string value
   * @return a pattern that matches values equal to {@code value}
   */
  public static StringValuePattern equalTo(String value) {
    return new EqualToPattern(value);
  }

  /**
   * Creates a {@link BinaryEqualToPattern} that matches binary content equal to the given byte
   * array.
   *
   * @param content the expected binary content
   * @return a pattern that matches binary content equal to {@code content}
   */
  public static BinaryEqualToPattern binaryEqualTo(byte[] content) {
    return new BinaryEqualToPattern(content);
  }

  /**
   * Creates a {@link BinaryEqualToPattern} that matches binary content equal to the given string.
   *
   * @param content the expected content as a string
   * @return a pattern that matches binary content equal to {@code content}
   */
  public static BinaryEqualToPattern binaryEqualTo(String content) {
    return new BinaryEqualToPattern(content);
  }

  /**
   * Creates a {@link StringValuePattern} that matches values equal to the given string, ignoring
   * case sensitivity.
   *
   * @param value the expected string value
   * @return a pattern that matches values equal to {@code value}, ignoring case
   */
  public static StringValuePattern equalToIgnoreCase(String value) {
    return new EqualToPattern(value, true);
  }

  /**
   * Creates a {@link StringValuePattern} that matches JSON values equal to the given string.
   *
   * @param value the expected JSON content as a string
   * @return a pattern that matches JSON equal to {@code value}
   */
  public static StringValuePattern equalToJson(String value) {
    return new EqualToJsonPattern(value, null, null);
  }

  /**
   * Creates a {@link StringValuePattern} that matches JSON values equal to the given string, with
   * additional options for ignoring array order and extra elements.
   *
   * @param value the expected JSON content as a string
   * @param ignoreArrayOrder whether to ignore the order of array elements
   * @param ignoreExtraElements whether to ignore extra elements not present in {@code value}
   * @return a pattern that matches JSON equal to {@code value} with the specified options
   */
  public static StringValuePattern equalToJson(
      String value, boolean ignoreArrayOrder, boolean ignoreExtraElements) {
    return new EqualToJsonPattern(value, ignoreArrayOrder, ignoreExtraElements);
  }

  /**
   * Creates a {@link StringValuePattern} that matches values using the given JSONPath expression.
   *
   * @param value the JSONPath expression
   * @return a pattern that matches JSON documents containing elements satisfying {@code value}
   */
  public static StringValuePattern matchingJsonPath(String value) {
    return new MatchesJsonPathPattern(value);
  }

  /**
   * Creates a {@link StringValuePattern} that matches values using the given JSONPath expression,
   * with an additional pattern applied to the matched value.
   *
   * @param value the JSONPath expression
   * @param valuePattern the value pattern to apply to the JSONPath result
   * @return a pattern that matches JSON documents where {@code value} resolves to something
   *     matching {@code valuePattern}
   */
  public static StringValuePattern matchingJsonPath(String value, StringValuePattern valuePattern) {
    return new MatchesJsonPathPattern(value, valuePattern);
  }

  /**
   * Creates a {@link StringValuePattern} that matches values against the given JSON Schema.
   *
   * @param schema the JSON Schema definition
   * @return a pattern that matches JSON documents conforming to {@code schema}
   */
  public static StringValuePattern matchingJsonSchema(String schema) {
    return new MatchesJsonSchemaPattern(schema);
  }

  /**
   * Creates a {@link StringValuePattern} that matches values against the given JSON Schema,
   * specifying the JSON Schema version.
   *
   * @param schema the JSON Schema definition
   * @param jsonSchemaVersion the version of the JSON Schema specification to use
   * @return a pattern that matches JSON documents conforming to {@code schema}
   */
  public static StringValuePattern matchingJsonSchema(
      String schema, JsonSchemaVersion jsonSchemaVersion) {
    return new MatchesJsonSchemaPattern(schema, jsonSchemaVersion);
  }

  /**
   * Creates an {@link EqualToXmlPattern} that matches XML values equal to the given string.
   *
   * @param value the expected XML content
   * @return a pattern that matches XML equal to {@code value}
   */
  public static EqualToXmlPattern equalToXml(String value) {
    return new EqualToXmlPattern(value);
  }

  /**
   * Creates an {@link EqualToXmlPattern} that matches XML values equal to the given string, with a
   * specific namespace awareness configuration.
   *
   * @param value the expected XML content
   * @param namespaceAwareness the namespace awareness mode to apply during comparison
   * @return a pattern that matches XML equal to {@code value} with the given namespace handling
   */
  public static EqualToXmlPattern equalToXml(
      String value, EqualToXmlPattern.NamespaceAwareness namespaceAwareness) {
    return new EqualToXmlPattern(value, null, null, null, null, null, namespaceAwareness);
  }

  /**
   * Creates an {@link EqualToXmlPattern} matcher for XML equality.
   *
   * @param value XML string to match against
   * @param enablePlaceholders whether placeholder tokens should be enabled
   * @return an XML equality matcher
   */
  public static EqualToXmlPattern equalToXml(String value, boolean enablePlaceholders) {
    return equalToXml(value, enablePlaceholders, false);
  }

  /**
   * Creates an {@link EqualToXmlPattern} matcher for XML equality.
   *
   * @param value XML string to match against
   * @param enablePlaceholders whether placeholder tokens should be enabled
   * @param ignoreOrderOfSameNode whether to ignore the order of repeated nodes
   * @return an XML equality matcher
   */
  public static EqualToXmlPattern equalToXml(
      String value, boolean enablePlaceholders, boolean ignoreOrderOfSameNode) {
    return new EqualToXmlPattern(value, enablePlaceholders, ignoreOrderOfSameNode);
  }

  /**
   * Creates an {@link EqualToXmlPattern} matcher for XML equality with custom placeholder
   * delimiters.
   *
   * @param value XML string to match against
   * @param enablePlaceholders whether placeholder tokens should be enabled
   * @param placeholderOpeningDelimiterRegex regex for placeholder opening delimiter
   * @param placeholderClosingDelimiterRegex regex for placeholder closing delimiter
   * @return an XML equality matcher
   */
  public static EqualToXmlPattern equalToXml(
      String value,
      boolean enablePlaceholders,
      String placeholderOpeningDelimiterRegex,
      String placeholderClosingDelimiterRegex) {
    return equalToXml(
        value,
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        false);
  }

  /**
   * Creates an {@link EqualToXmlPattern} matcher for XML equality with custom placeholder
   * delimiters.
   *
   * @param value XML string to match against
   * @param enablePlaceholders whether placeholder tokens should be enabled
   * @param placeholderOpeningDelimiterRegex regex for placeholder opening delimiter
   * @param placeholderClosingDelimiterRegex regex for placeholder closing delimiter
   * @param ignoreOrderOfSameNode whether to ignore the order of repeated nodes
   * @return an XML equality matcher
   */
  public static EqualToXmlPattern equalToXml(
      String value,
      boolean enablePlaceholders,
      String placeholderOpeningDelimiterRegex,
      String placeholderClosingDelimiterRegex,
      boolean ignoreOrderOfSameNode) {
    return equalToXml(
        value,
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        ignoreOrderOfSameNode,
        null);
  }

  /**
   * Creates an {@link EqualToXmlPattern} matcher for XML equality with namespace awareness.
   *
   * @param value XML string to match against
   * @param enablePlaceholders whether placeholder tokens should be enabled
   * @param placeholderOpeningDelimiterRegex regex for placeholder opening delimiter
   * @param placeholderClosingDelimiterRegex regex for placeholder closing delimiter
   * @param ignoreOrderOfSameNode whether to ignore the order of repeated nodes
   * @param namespaceAwareness namespace awareness configuration
   * @return an XML equality matcher
   */
  public static EqualToXmlPattern equalToXml(
      String value,
      boolean enablePlaceholders,
      String placeholderOpeningDelimiterRegex,
      String placeholderClosingDelimiterRegex,
      boolean ignoreOrderOfSameNode,
      EqualToXmlPattern.NamespaceAwareness namespaceAwareness) {
    return new EqualToXmlPattern(
        value,
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        null,
        ignoreOrderOfSameNode,
        namespaceAwareness);
  }

  /**
   * Creates a matcher for XPath expressions.
   *
   * @param value XPath expression
   * @return a matcher that verifies XML against the XPath
   */
  public static MatchesXPathPattern matchingXpath(String value) {
    return new MatchesXPathPattern(value, Collections.emptyMap());
  }

  /**
   * Creates a matcher for XPath expressions with namespace support.
   *
   * @param value XPath expression
   * @param namespaces map of namespace prefixes to URIs
   * @return a matcher that verifies XML against the XPath with namespaces
   */
  public static StringValuePattern matchingXpath(String value, Map<String, String> namespaces) {
    return new MatchesXPathPattern(value, namespaces);
  }

  /**
   * Creates a matcher for XPath expressions with an additional value pattern.
   *
   * @param value XPath expression
   * @param valuePattern expected value pattern for the matched content
   * @return a matcher that verifies XML against the XPath and value pattern
   */
  public static StringValuePattern matchingXpath(String value, StringValuePattern valuePattern) {
    return new MatchesXPathPattern(value, valuePattern);
  }

  /**
   * Creates a matcher for XPath expressions with a submatcher. This is typically used in
   * combination with date/time matchers to avoid explicit casting.
   *
   * @param value XPath expression
   * @param valuePattern expected value pattern for the matched content
   * @return a matcher that verifies XML against the XPath and value pattern
   */
  // Use this with the date/time matchers to avoid an explicit cast
  public static MatchesXPathPattern matchesXpathwithsubmatcher(
      String value, StringValuePattern valuePattern) {
    return new MatchesXPathPattern(value, valuePattern);
  }

  /**
   * Creates a pattern that matches when the given value is contained in the target string.
   *
   * @param value The substring to look for.
   * @return A {@link ContainsPattern} instance.
   */
  public static StringValuePattern containing(String value) {
    return new ContainsPattern(value);
  }

  /**
   * Creates a pattern that matches when the given value is NOT contained in the target string.
   *
   * @param value The substring that must not appear.
   * @return A {@link NegativeContainsPattern} instance.
   */
  public static StringValuePattern notContaining(String value) {
    return new NegativeContainsPattern(value);
  }

  /**
   * Creates a pattern that negates another pattern.
   *
   * @param unexpectedPattern The pattern that should NOT match.
   * @return A {@link NotPattern} instance.
   */
  public static StringValuePattern not(StringValuePattern unexpectedPattern) {
    return new NotPattern(unexpectedPattern);
  }

  /**
   * Creates a pattern that matches strings against the given regular expression.
   *
   * @param regex The regular expression to match.
   * @return A {@link RegexPattern} instance.
   */
  public static StringValuePattern matching(String regex) {
    return new RegexPattern(regex);
  }

  /**
   * Creates a pattern that matches strings NOT matching the given regular expression.
   *
   * @param regex The regular expression that should not match.
   * @return A {@link NegativeRegexPattern} instance.
   */
  public static StringValuePattern notMatching(String regex) {
    return new NegativeRegexPattern(regex);
  }

  /**
   * Creates a pattern that matches dates before the given date-time specification.
   *
   * @param dateTimeSpec The date-time expression (e.g. ISO-8601 string, "now").
   * @return A {@link BeforeDateTimePattern} instance.
   */
  public static BeforeDateTimePattern before(String dateTimeSpec) {
    return new BeforeDateTimePattern(dateTimeSpec);
  }

  /**
   * Creates a pattern that matches dates before the given {@link ZonedDateTime}.
   *
   * @param dateTime The date-time reference.
   * @return A {@link BeforeDateTimePattern} instance.
   */
  public static BeforeDateTimePattern before(ZonedDateTime dateTime) {
    return new BeforeDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches dates before the given {@link LocalDateTime}.
   *
   * @param dateTime The date-time reference.
   * @return A {@link BeforeDateTimePattern} instance.
   */
  public static BeforeDateTimePattern before(LocalDateTime dateTime) {
    return new BeforeDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches any date-time before the current time.
   *
   * @return A {@link BeforeDateTimePattern} instance.
   */
  public static BeforeDateTimePattern beforeNow() {
    return new BeforeDateTimePattern("now");
  }

  /**
   * Creates a pattern that matches when the date-time is equal to the given specification.
   *
   * @param dateTimeSpec The date-time expression (e.g. ISO-8601 string, "now").
   * @return An {@link EqualToDateTimePattern} instance.
   */
  public static EqualToDateTimePattern equalToDateTime(String dateTimeSpec) {
    return new EqualToDateTimePattern(dateTimeSpec);
  }

  /**
   * Creates a pattern that matches when the date-time is equal to the given {@link ZonedDateTime}.
   *
   * @param dateTime The exact date-time.
   * @return An {@link EqualToDateTimePattern} instance.
   */
  public static EqualToDateTimePattern equalToDateTime(ZonedDateTime dateTime) {
    return new EqualToDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches when the date-time is equal to the given {@link LocalDateTime}.
   *
   * @param dateTime The exact date-time.
   * @return An {@link EqualToDateTimePattern} instance.
   */
  public static EqualToDateTimePattern equalToDateTime(LocalDateTime dateTime) {
    return new EqualToDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches when the date-time corresponds to the current moment.
   *
   * @return An {@link EqualToDateTimePattern} instance.
   */
  public static EqualToDateTimePattern isNow() {
    return new EqualToDateTimePattern("now");
  }

  /**
   * Creates a pattern that matches when the date-time is after the given specification.
   *
   * @param dateTimeSpec The date-time expression (e.g. ISO-8601 string, "now").
   * @return An {@link AfterDateTimePattern} instance.
   */
  public static AfterDateTimePattern after(String dateTimeSpec) {
    return new AfterDateTimePattern(dateTimeSpec);
  }

  /**
   * Creates a pattern that matches when the date-time is after the given {@link ZonedDateTime}.
   *
   * @param dateTime The date-time reference.
   * @return An {@link AfterDateTimePattern} instance.
   */
  public static AfterDateTimePattern after(ZonedDateTime dateTime) {
    return new AfterDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches when the date-time is after the given {@link LocalDateTime}.
   *
   * @param dateTime The date-time reference.
   * @return An {@link AfterDateTimePattern} instance.
   */
  public static AfterDateTimePattern after(LocalDateTime dateTime) {
    return new AfterDateTimePattern(dateTime);
  }

  /**
   * Creates a pattern that matches any date-time after the current moment.
   *
   * @return An {@link AfterDateTimePattern} instance.
   */
  public static AfterDateTimePattern afterNow() {
    return new AfterDateTimePattern("now");
  }

  /**
   * Creates a pattern that matches when a field is absent.
   *
   * @return A singleton {@link AbsentPattern}.
   */
  public static StringValuePattern absent() {
    return AbsentPattern.ABSENT;
  }

  /**
   * Creates a logical AND pattern that combines multiple matchers. All provided patterns must
   * match.
   *
   * @param matchers The patterns to combine.
   * @return A {@link LogicalAnd} instance.
   */
  public static StringValuePattern and(StringValuePattern... matchers) {
    return new LogicalAnd(matchers);
  }

  /**
   * Or string value pattern.
   *
   * @param matchers the matchers
   * @return the string value pattern
   */
  public static StringValuePattern or(StringValuePattern... matchers) {
    return new LogicalOr(matchers);
  }

  /** Save mappings. */
  public void saveMappings() {
    admin.saveMappings();
  }

  /** Save all mappings. */
  public static void saveAllMappings() {
    defaultInstance.get().saveMappings();
  }

  /** Remove mappings. */
  public void removeMappings() {
    admin.resetMappings();
  }

  /** Remove all mappings. */
  public static void removeAllMappings() {
    defaultInstance.get().removeMappings();
  }

  /** Reset mappings. */
  public void resetMappings() {
    admin.resetAll();
  }

  /** Reset. */
  public static void reset() {
    defaultInstance.get().resetMappings();
  }

  /** Reset all requests. */
  public static void resetAllRequests() {
    defaultInstance.get().resetRequests();
  }

  /** Reset requests. */
  public void resetRequests() {
    admin.resetRequests();
  }

  /** Reset scenarios. */
  public void resetScenarios() {
    admin.resetScenarios();
  }

  /**
   * Reset scenario.
   *
   * @param name the name
   */
  public static void resetScenario(String name) {
    defaultInstance.get().resetScenarioState(name);
  }

  /**
   * Reset scenario state.
   *
   * @param name the name
   */
  public void resetScenarioState(String name) {
    admin.resetScenario(name);
  }

  /**
   * Sets scenario state.
   *
   * @param name the name
   * @param state the state
   */
  public static void setScenarioState(String name, String state) {
    defaultInstance.get().setSingleScenarioState(name, state);
  }

  /**
   * Sets single scenario state.
   *
   * @param name the name
   * @param state the state
   */
  public void setSingleScenarioState(String name, String state) {
    admin.setScenarioState(name, state);
  }

  /**
   * Gets all scenarios.
   *
   * @return the all scenarios
   */
  public static List<Scenario> getAllScenarios() {
    return defaultInstance.get().getScenarios();
  }

  /**
   * Gets scenarios.
   *
   * @return the scenarios
   */
  public List<Scenario> getScenarios() {
    return admin.getAllScenarios().getScenarios();
  }

  /** Reset all scenarios. */
  public static void resetAllScenarios() {
    defaultInstance.get().resetScenarios();
  }

  /** Reset to default mappings. */
  public void resetToDefaultMappings() {
    admin.resetToDefaultMappings();
  }

  /** Reset to default. */
  public static void resetToDefault() {
    defaultInstance.get().resetToDefaultMappings();
  }

  /**
   * Register stub mapping.
   *
   * @param mappingBuilder the mapping builder
   * @return the stub mapping
   */
  public StubMapping register(MappingBuilder mappingBuilder) {
    StubMapping mapping = mappingBuilder.build();
    register(mapping);
    return mapping;
  }

  /**
   * Register.
   *
   * @param mapping the mapping
   */
  public void register(StubMapping mapping) {
    admin.addStubMapping(mapping);
  }

  /**
   * Edit stub mapping.
   *
   * @param mappingBuilder the mapping builder
   */
  public void editStubMapping(MappingBuilder mappingBuilder) {
    admin.editStubMapping(mappingBuilder.build());
  }

  /**
   * Remove stub mapping.
   *
   * @param mappingBuilder the mapping builder
   */
  public void removeStubMapping(MappingBuilder mappingBuilder) {
    admin.removeStubMapping(mappingBuilder.build());
  }

  /**
   * Remove stub mapping.
   *
   * @param stubMapping the stub mapping
   */
  public void removeStubMapping(StubMapping stubMapping) {
    admin.removeStubMapping(stubMapping);
  }

  /**
   * Remove stub mapping.
   *
   * @param id the id
   */
  public void removeStubMapping(UUID id) {
    admin.removeStubMapping(id);
  }

  /**
   * All stub mappings list stub mappings result.
   *
   * @return the list stub mappings result
   */
  public ListStubMappingsResult allStubMappings() {
    return admin.listAllStubMappings();
  }

  /**
   * Gets stub mapping.
   *
   * @param id the id
   * @return the stub mapping
   */
  public SingleStubMappingResult getStubMapping(UUID id) {
    return admin.getStubMapping(id);
  }

  /**
   * Url equal to url pattern.
   *
   * @param testUrl the test url
   * @return the url pattern
   */
  public static UrlPattern urlEqualTo(String testUrl) {
    return new UrlPattern(equalTo(testUrl), false);
  }

  /**
   * Url matching url pattern.
   *
   * @param urlRegex the url regex
   * @return the url pattern
   */
  public static UrlPattern urlMatching(String urlRegex) {
    return new UrlPattern(matching(urlRegex), true);
  }

  /**
   * Url path equal to url path pattern.
   *
   * @param testUrl the test url
   * @return the url path pattern
   */
  public static UrlPathPattern urlPathEqualTo(String testUrl) {
    return new UrlPathPattern(equalTo(testUrl), false);
  }

  /**
   * Url path matching url path pattern.
   *
   * @param urlRegex the url regex
   * @return the url path pattern
   */
  public static UrlPathPattern urlPathMatching(String urlRegex) {
    return new UrlPathPattern(matching(urlRegex), true);
  }

  /**
   * Url path template url path pattern.
   *
   * @param pathTemplate the path template
   * @return the url path pattern
   */
  public static UrlPathPattern urlPathTemplate(String pathTemplate) {
    return new UrlPathTemplatePattern(pathTemplate);
  }

  /**
   * Having exactly multi value pattern.
   *
   * @param valuePatterns the value patterns
   * @return the multi value pattern
   */
  public static MultiValuePattern havingExactly(final StringValuePattern... valuePatterns) {
    if (valuePatterns.length == 0) {
      return noValues();
    }
    return new ExactMatchMultiValuePattern(Stream.of(valuePatterns).collect(Collectors.toList()));
  }

  /**
   * Having exactly multi value pattern.
   *
   * @param values the values
   * @return the multi value pattern
   */
  public static MultiValuePattern havingExactly(String... values) {
    return havingExactly(Stream.of(values).map(EqualToPattern::new).toArray(EqualToPattern[]::new));
  }

  /**
   * Including multi value pattern.
   *
   * @param valuePatterns the value patterns
   * @return the multi value pattern
   */
  public static MultiValuePattern including(final StringValuePattern... valuePatterns) {
    if (valuePatterns.length == 0) {
      return noValues();
    }
    return new IncludesMatchMultiValuePattern(
        Stream.of(valuePatterns).collect(Collectors.toList()));
  }

  /**
   * Including multi value pattern.
   *
   * @param values the values
   * @return the multi value pattern
   */
  public static MultiValuePattern including(String... values) {
    return including(Stream.of(values).map(EqualToPattern::new).toArray(EqualToPattern[]::new));
  }

  /**
   * No values multi value pattern.
   *
   * @return the multi value pattern
   */
  public static MultiValuePattern noValues() {
    return MultiValuePattern.of(absent());
  }

  /**
   * Any url url pattern.
   *
   * @return the url pattern
   */
  public static UrlPattern anyUrl() {
    return UrlPattern.ANY;
  }

  /**
   * Less than count matching strategy.
   *
   * @param expected the expected
   * @return the count matching strategy
   */
  public static CountMatchingStrategy lessThan(int expected) {
    return new CountMatchingStrategy(CountMatchingStrategy.LESS_THAN, expected);
  }

  /**
   * Less than or exactly count matching strategy.
   *
   * @param expected the expected
   * @return the count matching strategy
   */
  public static CountMatchingStrategy lessThanOrExactly(int expected) {
    return new CountMatchingStrategy(CountMatchingStrategy.LESS_THAN_OR_EQUAL, expected);
  }

  /**
   * Exactly count matching strategy.
   *
   * @param expected the expected
   * @return the count matching strategy
   */
  public static CountMatchingStrategy exactly(int expected) {
    return new CountMatchingStrategy(CountMatchingStrategy.EQUAL_TO, expected);
  }

  /**
   * More than or exactly count matching strategy.
   *
   * @param expected the expected
   * @return the count matching strategy
   */
  public static CountMatchingStrategy moreThanOrExactly(int expected) {
    return new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN_OR_EQUAL, expected);
  }

  /**
   * More than count matching strategy.
   *
   * @param expected the expected
   * @return the count matching strategy
   */
  public static CountMatchingStrategy moreThan(int expected) {
    return new CountMatchingStrategy(CountMatchingStrategy.GREATER_THAN, expected);
  }

  /**
   * Get mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder get(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.GET, urlPattern);
  }

  /**
   * Post mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder post(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.POST, urlPattern);
  }

  /**
   * Put mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder put(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.PUT, urlPattern);
  }

  /**
   * Delete mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder delete(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.DELETE, urlPattern);
  }

  /**
   * Patch mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder patch(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.PATCH, urlPattern);
  }

  /**
   * Head mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder head(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.HEAD, urlPattern);
  }

  /**
   * Options mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder options(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.OPTIONS, urlPattern);
  }

  /**
   * Trace mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder trace(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.TRACE, urlPattern);
  }

  /**
   * Any mapping builder.
   *
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder any(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.ANY, urlPattern);
  }

  /**
   * A mapping builder that can be used for both GET and HEAD http method. Returns a response body
   * in case for GET and not in case of HEAD method. In case of tie the request is treated as a GET
   * request
   *
   * @param urlPattern for the specified method
   * @return a mapping builder for {@link RequestMethod#GET_OR_HEAD} http method
   */
  public static MappingBuilder getOrHead(UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.GET_OR_HEAD, urlPattern);
  }

  /**
   * Request mapping builder.
   *
   * @param method the method
   * @param urlPattern the url pattern
   * @return the mapping builder
   */
  public static MappingBuilder request(String method, UrlPattern urlPattern) {
    return new BasicMappingBuilder(RequestMethod.fromString(method), urlPattern);
  }

  /**
   * Request matching mapping builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @return the mapping builder
   */
  public static MappingBuilder requestMatching(String customRequestMatcherName) {
    return new BasicMappingBuilder(customRequestMatcherName, Parameters.empty());
  }

  /**
   * Request matching mapping builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @param parameters the parameters
   * @return the mapping builder
   */
  public static MappingBuilder requestMatching(
      String customRequestMatcherName, Parameters parameters) {
    return new BasicMappingBuilder(customRequestMatcherName, parameters);
  }

  /**
   * Request matching mapping builder.
   *
   * @param requestMatcher the request matcher
   * @return the mapping builder
   */
  public static MappingBuilder requestMatching(ValueMatcher<Request> requestMatcher) {
    return new BasicMappingBuilder(requestMatcher);
  }

  /**
   * A response response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder aresponse() {
    return new ResponseDefinitionBuilder();
  }

  /**
   * Ok response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder ok() {
    return aresponse().withStatus(200);
  }

  /**
   * Ok response definition builder.
   *
   * @param body the body
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder ok(String body) {
    return aresponse().withStatus(200).withBody(body);
  }

  /**
   * Ok for content type response definition builder.
   *
   * @param contentType the content type
   * @param body the body
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder okForContentType(String contentType, String body) {
    return aresponse().withStatus(200).withHeader(CONTENT_TYPE, contentType).withBody(body);
  }

  /**
   * Ok json response definition builder.
   *
   * @param body the body
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder okJson(String body) {
    return okForContentType("application/json", body);
  }

  /**
   * Ok xml response definition builder.
   *
   * @param body the body
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder okXml(String body) {
    return okForContentType("application/xml", body);
  }

  /**
   * Ok text xml response definition builder.
   *
   * @param body the body
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder okTextXml(String body) {
    return okForContentType("text/xml", body);
  }

  /**
   * Json response response definition builder.
   *
   * @param body the body
   * @param status the status
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder jsonResponse(String body, int status) {
    return aresponse()
        .withStatus(status)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody(body);
  }

  /**
   * Json response response definition builder.
   *
   * @param body the body
   * @param status the status
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder jsonResponse(Object body, int status) {
    return jsonResponse(Json.write(body), status);
  }

  /**
   * Proxy all to mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder proxyAllTo(String url) {
    return any(anyUrl()).willReturn(aresponse().proxiedFrom(url));
  }

  /**
   * Get mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder get(String url) {
    return get(urlEqualTo(url));
  }

  /**
   * Post mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder post(String url) {
    return post(urlEqualTo(url));
  }

  /**
   * Put mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder put(String url) {
    return put(urlEqualTo(url));
  }

  /**
   * Delete mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder delete(String url) {
    return delete(urlEqualTo(url));
  }

  /**
   * Patch mapping builder.
   *
   * @param url the url
   * @return the mapping builder
   */
  public static MappingBuilder patch(String url) {
    return patch(urlEqualTo(url));
  }

  /**
   * Created response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder created() {
    return aresponse().withStatus(201);
  }

  /**
   * No content response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder noContent() {
    return aresponse().withStatus(204);
  }

  /**
   * Permanent redirect response definition builder.
   *
   * @param location the location
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder permanentRedirect(String location) {
    return aresponse().withStatus(301).withHeader(LOCATION, location);
  }

  /**
   * Temporary redirect response definition builder.
   *
   * @param location the location
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder temporaryRedirect(String location) {
    return aresponse().withStatus(302).withHeader(LOCATION, location);
  }

  /**
   * See other response definition builder.
   *
   * @param location the location
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder seeOther(String location) {
    return aresponse().withStatus(303).withHeader(LOCATION, location);
  }

  /**
   * Bad request response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder badRequest() {
    return aresponse().withStatus(400);
  }

  /**
   * Bad request entity response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder badRequestEntity() {
    return aresponse().withStatus(422);
  }

  /**
   * Unauthorized response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder unauthorized() {
    return aresponse().withStatus(401);
  }

  /**
   * Forbidden response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder forbidden() {
    return aresponse().withStatus(403);
  }

  /**
   * Not found response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder notFound() {
    return aresponse().withStatus(404);
  }

  /**
   * Server error response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder serverError() {
    return aresponse().withStatus(500);
  }

  /**
   * Service unavailable response definition builder.
   *
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder serviceUnavailable() {
    return aresponse().withStatus(503);
  }

  /**
   * Status response definition builder.
   *
   * @param status the status
   * @return the response definition builder
   */
  public static ResponseDefinitionBuilder status(int status) {
    return aresponse().withStatus(status);
  }

  /**
   * Verify that.
   *
   * @param requestPatternBuilder the request pattern builder
   */
  public void verifyThat(RequestPatternBuilder requestPatternBuilder) {
    verifyThat(moreThanOrExactly(1), requestPatternBuilder);
  }

  /**
   * Verify that.
   *
   * @param expectedCount the expected count
   * @param requestPatternBuilder the request pattern builder
   */
  public void verifyThat(int expectedCount, RequestPatternBuilder requestPatternBuilder) {
    verifyThat(exactly(expectedCount), requestPatternBuilder);
  }

  /**
   * Verify that.
   *
   * @param expectedCount the expected count
   * @param requestPatternBuilder the request pattern builder
   */
  public void verifyThat(
      CountMatchingStrategy expectedCount, RequestPatternBuilder requestPatternBuilder) {
    final RequestPattern requestPattern = requestPatternBuilder.build();

    int actualCount;
    if (requestPattern.hasInlineCustomMatcher()) {
      List<LoggedRequest> requests =
          admin.findRequestsMatching(RequestPattern.everything()).getRequests();
      actualCount = (int) requests.stream().filter(thatMatch(requestPattern)).count();
    } else {
      VerificationResult result = admin.countRequestsMatching(requestPattern);
      result.assertRequestJournalEnabled();
      actualCount = result.getCount();
    }

    if (!expectedCount.match(actualCount)) {
      throw actualCount == 0
          ? verificationExceptionForNearMisses(requestPatternBuilder, requestPattern)
          : new VerificationException(requestPattern, expectedCount, actualCount);
    }
  }

  private VerificationException verificationExceptionForNearMisses(
      RequestPatternBuilder requestPatternBuilder, RequestPattern requestPattern) {
    List<NearMiss> nearMisses = findAllNearMissesFor(requestPatternBuilder);
    if (!nearMisses.isEmpty()) {
      Diff diff = new Diff(requestPattern, nearMisses.get(0).getRequest());
      return VerificationException.forUnmatchedRequestPattern(diff);
    }

    return new VerificationException(requestPattern, find(allRequests()));
  }

  /**
   * Verify.
   *
   * @param requestPatternBuilder the request pattern builder
   */
  public static void verify(RequestPatternBuilder requestPatternBuilder) {
    defaultInstance.get().verifyThat(requestPatternBuilder);
  }

  /**
   * Verify.
   *
   * @param count the count
   * @param requestPatternBuilder the request pattern builder
   */
  public static void verify(int count, RequestPatternBuilder requestPatternBuilder) {
    defaultInstance.get().verifyThat(count, requestPatternBuilder);
  }

  /**
   * Verify.
   *
   * @param countMatchingStrategy the count matching strategy
   * @param requestPatternBuilder the request pattern builder
   */
  public static void verify(
      CountMatchingStrategy countMatchingStrategy, RequestPatternBuilder requestPatternBuilder) {
    defaultInstance.get().verifyThat(countMatchingStrategy, requestPatternBuilder);
  }

  /**
   * Find list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  public List<LoggedRequest> find(RequestPatternBuilder requestPatternBuilder) {
    FindRequestsResult result = admin.findRequestsMatching(requestPatternBuilder.build());
    result.assertRequestJournalEnabled();
    return result.getRequests();
  }

  /**
   * Find all list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  public static List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
    return defaultInstance.get().find(requestPatternBuilder);
  }

  /**
   * Gets all serve events.
   *
   * @return the all serve events
   */
  public static List<ServeEvent> getAllServeEvents() {
    return defaultInstance.get().getServeEvents();
  }

  /**
   * Gets serve events.
   *
   * @return the serve events
   */
  public List<ServeEvent> getServeEvents() {
    return admin.getServeEvents().getRequests();
  }

  /**
   * Gets all serve events.
   *
   * @param query the query
   * @return the all serve events
   */
  public static List<ServeEvent> getAllServeEvents(ServeEventQuery query) {
    return defaultInstance.get().getServeEvents(query);
  }

  /**
   * Gets serve events.
   *
   * @param query the query
   * @return the serve events
   */
  public List<ServeEvent> getServeEvents(ServeEventQuery query) {
    return admin.getServeEvents(query).getRequests();
  }

  /**
   * Remove serve event.
   *
   * @param eventId the event id
   */
  public static void removeServeEvent(UUID eventId) {
    defaultInstance.get().removeEvent(eventId);
  }

  /**
   * Remove event.
   *
   * @param eventId the event id
   */
  public void removeEvent(UUID eventId) {
    admin.removeServeEvent(eventId);
  }

  /**
   * Remove events list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  public List<ServeEvent> removeEvents(RequestPatternBuilder requestPatternBuilder) {
    return admin.removeServeEventsMatching(requestPatternBuilder.build()).getServeEvents();
  }

  /**
   * Remove serve events list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  public static List<ServeEvent> removeServeEvents(RequestPatternBuilder requestPatternBuilder) {
    return defaultInstance.get().removeEvents(requestPatternBuilder);
  }

  /**
   * Remove events by stub metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  public static List<ServeEvent> removeEventsByStubMetadata(StringValuePattern pattern) {
    return defaultInstance.get().removeEventsByMetadata(pattern);
  }

  /**
   * Remove events by metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  public List<ServeEvent> removeEventsByMetadata(StringValuePattern pattern) {
    return admin.removeServeEventsForStubsMatchingMetadata(pattern).getServeEvents();
  }

  /**
   * Gets requested for.
   *
   * @param urlPattern the url pattern
   * @return the requested for
   */
  public static RequestPatternBuilder getRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.GET, urlPattern);
  }

  /**
   * Post requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder postRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.POST, urlPattern);
  }

  /**
   * Put requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder putRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.PUT, urlPattern);
  }

  /**
   * Delete requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder deleteRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.DELETE, urlPattern);
  }

  /**
   * Patch requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder patchRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.PATCH, urlPattern);
  }

  /**
   * Head requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder headRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.HEAD, urlPattern);
  }

  /**
   * Options requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder optionsRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.OPTIONS, urlPattern);
  }

  /**
   * Trace requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder traceRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.TRACE, urlPattern);
  }

  /**
   * Any requested for request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder anyRequestedFor(UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.ANY, urlPattern);
  }

  /**
   * Requested for request pattern builder.
   *
   * @param method the method
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public static RequestPatternBuilder requestedFor(String method, UrlPattern urlPattern) {
    return new RequestPatternBuilder(RequestMethod.fromString(method), urlPattern);
  }

  /**
   * Request made for request pattern builder.
   *
   * @param customMatcherName the custom matcher name
   * @param parameters the parameters
   * @return the request pattern builder
   */
  public static RequestPatternBuilder requestMadeFor(
      String customMatcherName, Parameters parameters) {
    return RequestPatternBuilder.forCustomMatcher(customMatcherName, parameters);
  }

  /**
   * Request made for request pattern builder.
   *
   * @param requestMatcher the request matcher
   * @return the request pattern builder
   */
  public static RequestPatternBuilder requestMadeFor(ValueMatcher<Request> requestMatcher) {
    return RequestPatternBuilder.forCustomMatcher(requestMatcher);
  }

  /**
   * Sets global fixed delay.
   *
   * @param milliseconds the milliseconds
   */
  public static void setGlobalFixedDelay(int milliseconds) {
    defaultInstance.get().setGlobalFixedDelayVariable(milliseconds);
  }

  /**
   * Sets global fixed delay variable.
   *
   * @param milliseconds the milliseconds
   */
  public void setGlobalFixedDelayVariable(int milliseconds) {
    GlobalSettings settings = settingsStore.get().copy().fixedDelay(milliseconds).build();
    updateGlobalSettings(settings);
  }

  /**
   * Sets global random delay.
   *
   * @param distribution the distribution
   */
  public static void setGlobalRandomDelay(DelayDistribution distribution) {
    defaultInstance.get().setGlobalRandomDelayVariable(distribution);
  }

  /**
   * Sets global random delay variable.
   *
   * @param distribution the distribution
   */
  public void setGlobalRandomDelayVariable(DelayDistribution distribution) {
    GlobalSettings settings = settingsStore.get().copy().delayDistribution(distribution).build();
    updateGlobalSettings(settings);
  }

  /**
   * Update settings.
   *
   * @param settings the settings
   */
  public static void updateSettings(GlobalSettings settings) {
    defaultInstance.get().updateGlobalSettings(settings);
  }

  /**
   * Update global settings.
   *
   * @param settings the settings
   */
  public void updateGlobalSettings(GlobalSettings settings) {
    settingsStore.set(settings);
    admin.updateGlobalSettings(settings);
  }

  /** Shutdown. */
  public void shutdown() {
    admin.shutdownServer();
  }

  /** Shutdown server. */
  public static void shutdownServer() {
    defaultInstance.get().shutdown();
  }

  /**
   * Find near misses for all unmatched list.
   *
   * @return the list
   */
  public static List<NearMiss> findNearMissesForAllUnmatched() {
    return defaultInstance.get().findNearMissesForAllUnmatchedRequests();
  }

  /**
   * Find near misses for all unmatched requests list.
   *
   * @return the list
   */
  public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
    FindNearMissesResult nearMissesResult = admin.findNearMissesForUnmatchedRequests();
    return nearMissesResult.getNearMisses();
  }

  /**
   * Find unmatched requests list.
   *
   * @return the list
   */
  public static List<LoggedRequest> findUnmatchedRequests() {
    return defaultInstance.get().findAllUnmatchedRequests();
  }

  /**
   * Find all unmatched requests list.
   *
   * @return the list
   */
  public List<LoggedRequest> findAllUnmatchedRequests() {
    FindRequestsResult unmatchedResult = admin.findUnmatchedRequests();
    return unmatchedResult.getRequests();
  }

  /**
   * Find near misses for list.
   *
   * @param loggedRequest the logged request
   * @return the list
   */
  public static List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
    return defaultInstance.get().findTopNearMissesFor(loggedRequest);
  }

  /**
   * Find top near misses for list.
   *
   * @param loggedRequest the logged request
   * @return the list
   */
  public List<NearMiss> findTopNearMissesFor(LoggedRequest loggedRequest) {
    FindNearMissesResult nearMissesResult = admin.findTopNearMissesFor(loggedRequest);
    return nearMissesResult.getNearMisses();
  }

  /**
   * Find all near misses for list.
   *
   * @param requestPatternBuilder the request pattern builder
   * @return the list
   */
  public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
    FindNearMissesResult nearMissesResult =
        admin.findTopNearMissesFor(requestPatternBuilder.build());
    return nearMissesResult.getNearMisses();
  }

  /**
   * Load mappings from.
   *
   * @param rootDir the root dir
   */
  public void loadMappingsFrom(String rootDir) {
    loadMappingsFrom(new File(rootDir));
  }

  /**
   * Load mappings from.
   *
   * @param rootDir the root dir
   */
  public void loadMappingsFrom(File rootDir) {
    FileSource mappingsSource = new SingleRootFileSource(rootDir);
    new RemoteMappingsLoader(mappingsSource, this).load();
  }

  /**
   * Snapshot record list.
   *
   * @return the list
   */
  public static List<StubMapping> snapshotRecord() {
    return defaultInstance.get().takeSnapshotRecording();
  }

  /**
   * Snapshot record list.
   *
   * @param spec the spec
   * @return the list
   */
  public static List<StubMapping> snapshotRecord(RecordSpecBuilder spec) {
    return defaultInstance.get().takeSnapshotRecording(spec);
  }

  /**
   * Take snapshot recording list.
   *
   * @return the list
   */
  public List<StubMapping> takeSnapshotRecording() {
    return admin.snapshotRecord().getStubMappings();
  }

  /**
   * Take snapshot recording list.
   *
   * @param spec the spec
   * @return the list
   */
  public List<StubMapping> takeSnapshotRecording(RecordSpecBuilder spec) {
    return admin.snapshotRecord(spec.build()).getStubMappings();
  }

  /**
   * A multipart multipart value pattern builder.
   *
   * @return the multipart value pattern builder
   */
  public static MultipartValuePatternBuilder amultipart() {
    return new MultipartValuePatternBuilder();
  }

  /**
   * A multipart multipart value pattern builder.
   *
   * @param name the name
   * @return the multipart value pattern builder
   */
  public static MultipartValuePatternBuilder amultipart(String name) {
    return new MultipartValuePatternBuilder(name);
  }

  /**
   * Start recording.
   *
   * @param targetBaseUrl the target base url
   */
  public static void startRecording(String targetBaseUrl) {
    defaultInstance.get().startStubRecording(targetBaseUrl);
  }

  /** Start recording. */
  public static void startRecording() {
    defaultInstance.get().startStubRecording();
  }

  /**
   * Start recording.
   *
   * @param spec the spec
   */
  public static void startRecording(RecordSpecBuilder spec) {
    defaultInstance.get().startStubRecording(spec);
  }

  /**
   * Start stub recording.
   *
   * @param targetBaseUrl the target base url
   */
  public void startStubRecording(String targetBaseUrl) {
    admin.startRecording(targetBaseUrl);
  }

  /** Start stub recording. */
  public void startStubRecording() {
    admin.startRecording(RecordSpec.DEFAULTS);
  }

  /**
   * Start stub recording.
   *
   * @param spec the spec
   */
  public void startStubRecording(RecordSpecBuilder spec) {
    admin.startRecording(spec.build());
  }

  /**
   * Stop recording snapshot record result.
   *
   * @return the snapshot record result
   */
  public static SnapshotRecordResult stopRecording() {
    return defaultInstance.get().stopStubRecording();
  }

  /**
   * Stop stub recording snapshot record result.
   *
   * @return the snapshot record result
   */
  public SnapshotRecordResult stopStubRecording() {
    return admin.stopRecording();
  }

  /**
   * Gets recording status.
   *
   * @return the recording status
   */
  public static RecordingStatusResult getRecordingStatus() {
    return defaultInstance.get().getStubRecordingStatus();
  }

  /**
   * Gets stub recording status.
   *
   * @return the stub recording status
   */
  public RecordingStatusResult getStubRecordingStatus() {
    return admin.getRecordingStatus();
  }

  /**
   * Record spec record spec builder.
   *
   * @return the record spec builder
   */
  public static RecordSpecBuilder recordSpec() {
    return new RecordSpecBuilder();
  }

  /**
   * Find all stubs by metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  public List<StubMapping> findAllStubsByMetadata(StringValuePattern pattern) {
    return admin.findAllStubsByMetadata(pattern).getMappings();
  }

  /**
   * Find stubs by metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  public static List<StubMapping> findStubsByMetadata(StringValuePattern pattern) {
    return defaultInstance.get().findAllStubsByMetadata(pattern);
  }

  /**
   * Remove stubs by metadata pattern.
   *
   * @param pattern the pattern
   */
  public void removeStubsByMetadataPattern(StringValuePattern pattern) {
    admin.removeStubsByMetadata(pattern);
  }

  /**
   * Remove stubs by metadata.
   *
   * @param pattern the pattern
   */
  public static void removeStubsByMetadata(StringValuePattern pattern) {
    defaultInstance.get().removeStubsByMetadataPattern(pattern);
  }

  /**
   * Import stub mappings.
   *
   * @param stubImport the stub import
   */
  public void importStubMappings(StubImport stubImport) {
    admin.importStubs(stubImport);
  }

  /**
   * Import stub mappings.
   *
   * @param stubImport the stub import
   */
  public void importStubMappings(StubImportBuilder stubImport) {
    importStubMappings(stubImport.build());
  }

  /**
   * Import stubs.
   *
   * @param stubImport the stub import
   */
  public static void importStubs(StubImportBuilder stubImport) {
    importStubs(stubImport.build());
  }

  /**
   * Import stubs.
   *
   * @param stubImport the stub import
   */
  public static void importStubs(StubImport stubImport) {
    defaultInstance.get().importStubMappings(stubImport);
  }

  /**
   * Gets global settings.
   *
   * @return the global settings
   */
  public GlobalSettings getGlobalSettings() {
    return admin.getGlobalSettings().getSettings();
  }

  /**
   * Gets settings.
   *
   * @return the settings
   */
  public static GlobalSettings getSettings() {
    return defaultInstance.get().getGlobalSettings();
  }

  /** The enum Json schema version. */
  public enum JsonSchemaVersion {
    /** V 4 json schema version. */
    V4,
    /** V 6 json schema version. */
    V6,
    /** V 7 json schema version. */
    V7,
    /** V 201909 json schema version. */
    V201909,
    /** V 202012 json schema version. */
    V202012;

    /** The constant DEFAULT. */
    public static final JsonSchemaVersion DEFAULT = V202012;

    /**
     * To version flag spec version . version flag.
     *
     * @return the spec version . version flag
     */
    public SpecVersion.VersionFlag toVersionFlag() {
      switch (this) {
        case V4:
          return SpecVersion.VersionFlag.V4;
        case V6:
          return SpecVersion.VersionFlag.V6;
        case V7:
          return SpecVersion.VersionFlag.V7;
        case V201909:
          return SpecVersion.VersionFlag.V201909;
        case V202012:
          return SpecVersion.VersionFlag.V202012;
        default:
          throw new IllegalArgumentException("Unknown schema version: " + this);
      }
    }
  }
}
