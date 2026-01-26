/*
 * Copyright (C) 2026 Thomas Akehurst
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
package org.wiremock.url.jackson2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.wiremock.url.AbsoluteUri;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.Authority;
import org.wiremock.url.Fragment;
import org.wiremock.url.Host;
import org.wiremock.url.HostAndPort;
import org.wiremock.url.OpaqueUri;
import org.wiremock.url.Origin;
import org.wiremock.url.Password;
import org.wiremock.url.Path;
import org.wiremock.url.PathAndQuery;
import org.wiremock.url.Port;
import org.wiremock.url.Query;
import org.wiremock.url.QueryParamKey;
import org.wiremock.url.QueryParamValue;
import org.wiremock.url.RelativeUrl;
import org.wiremock.url.Scheme;
import org.wiremock.url.SchemeRelativeUrl;
import org.wiremock.url.Segment;
import org.wiremock.url.ServersideAbsoluteUrl;
import org.wiremock.url.Uri;
import org.wiremock.url.Url;
import org.wiremock.url.UrlWithAuthority;
import org.wiremock.url.UserInfo;
import org.wiremock.url.Username;

class WireMockUrlModuleSerializationTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new WireMockUrlModule());
  }

  @Nested
  class Deserialization {

    @Test
    void deserializesAbsoluteUri() throws JsonProcessingException {
      String json = "\"https://example.com/path?query=value#fragment\"";
      AbsoluteUri result = objectMapper.readValue(json, AbsoluteUri.class);
      assertThat(result.toString()).isEqualTo("https://example.com/path?query=value#fragment");
    }

    @Test
    void deserializesAbsoluteUrl() throws JsonProcessingException {
      String json = "\"https://user:pass@example.com:8080/path\"";
      AbsoluteUrl result = objectMapper.readValue(json, AbsoluteUrl.class);
      assertThat(result.toString()).isEqualTo("https://user:pass@example.com:8080/path");
      assertThat(result.getScheme().toString()).isEqualTo("https");
    }

    @Test
    void deserializesAuthority() throws JsonProcessingException {
      String json = "\"user:pass@example.com:8080\"";
      Authority result = objectMapper.readValue(json, Authority.class);
      assertThat(result.toString()).isEqualTo("user:pass@example.com:8080");
      assertThat(result.getHost().toString()).isEqualTo("example.com");
    }

    @Test
    void deserializesFragment() throws JsonProcessingException {
      String json = "\"section-1\"";
      Fragment result = objectMapper.readValue(json, Fragment.class);
      assertThat(result.toString()).isEqualTo("section-1");
    }

    @Test
    void deserializesHost() throws JsonProcessingException {
      String json = "\"example.com\"";
      Host result = objectMapper.readValue(json, Host.class);
      assertThat(result.toString()).isEqualTo("example.com");
    }

    @Test
    void deserializesHostWithIPv6() throws JsonProcessingException {
      String json = "\"[::1]\"";
      Host result = objectMapper.readValue(json, Host.class);
      assertThat(result.toString()).isEqualTo("[::1]");
    }

    @Test
    void deserializesHostAndPort() throws JsonProcessingException {
      String json = "\"example.com:8080\"";
      HostAndPort result = objectMapper.readValue(json, HostAndPort.class);
      assertThat(result.toString()).isEqualTo("example.com:8080");
      assertThat(result.getHost().toString()).isEqualTo("example.com");
    }

    @Test
    void deserializesOpaqueUri() throws JsonProcessingException {
      String json = "\"mailto:user@example.com\"";
      OpaqueUri result = objectMapper.readValue(json, OpaqueUri.class);
      assertThat(result.toString()).isEqualTo("mailto:user@example.com");
    }

    @Test
    void deserializesOrigin() throws JsonProcessingException {
      String json = "\"https://example.com:8080\"";
      Origin result = objectMapper.readValue(json, Origin.class);
      assertThat(result.toString()).isEqualTo("https://example.com:8080");
    }

    @Test
    void deserializesPassword() throws JsonProcessingException {
      String json = "\"secret123\"";
      Password result = objectMapper.readValue(json, Password.class);
      assertThat(result.toString()).isEqualTo("secret123");
    }

    @Test
    void deserializesPath() throws JsonProcessingException {
      String json = "\"/api/v1/users\"";
      Path result = objectMapper.readValue(json, Path.class);
      assertThat(result.toString()).isEqualTo("/api/v1/users");
    }

    @Test
    void deserializesPathAndQuery() throws JsonProcessingException {
      String json = "\"/path?key=value\"";
      PathAndQuery result = objectMapper.readValue(json, PathAndQuery.class);
      assertThat(result.toString()).isEqualTo("/path?key=value");
    }

    @Test
    void deserializesPort() throws JsonProcessingException {
      String json = "\"8080\"";
      Port result = objectMapper.readValue(json, Port.class);
      assertThat(result.toString()).isEqualTo("8080");
      assertThat(result.getIntValue()).isEqualTo(8080);
    }

    @Test
    void deserializesQuery() throws JsonProcessingException {
      String json = "\"key1=value1&key2=value2\"";
      Query result = objectMapper.readValue(json, Query.class);
      assertThat(result.toString()).isEqualTo("key1=value1&key2=value2");
    }

    @Test
    void deserializesQueryParamKey() throws JsonProcessingException {
      String json = "\"paramName\"";
      QueryParamKey result = objectMapper.readValue(json, QueryParamKey.class);
      assertThat(result.toString()).isEqualTo("paramName");
    }

    @Test
    void deserializesQueryParamValue() throws JsonProcessingException {
      String json = "\"paramValue\"";
      QueryParamValue result = objectMapper.readValue(json, QueryParamValue.class);
      assertThat(result.toString()).isEqualTo("paramValue");
    }

    @Test
    void deserializesRelativeUrl() throws JsonProcessingException {
      String json = "\"/path/to/resource?query=value\"";
      RelativeUrl result = objectMapper.readValue(json, RelativeUrl.class);
      assertThat(result.toString()).isEqualTo("/path/to/resource?query=value");
    }

    @Test
    void deserializesScheme() throws JsonProcessingException {
      String json = "\"https\"";
      Scheme result = objectMapper.readValue(json, Scheme.class);
      assertThat(result.toString()).isEqualTo("https");
    }

    @Test
    void deserializesSchemeRelativeUrl() throws JsonProcessingException {
      String json = "\"//example.com/path\"";
      SchemeRelativeUrl result = objectMapper.readValue(json, SchemeRelativeUrl.class);
      assertThat(result.toString()).isEqualTo("//example.com/path");
    }

    @Test
    void deserializesSegment() throws JsonProcessingException {
      String json = "\"path-segment\"";
      Segment result = objectMapper.readValue(json, Segment.class);
      assertThat(result.toString()).isEqualTo("path-segment");
    }

    @Test
    void deserializesServersideAbsoluteUrl() throws JsonProcessingException {
      String json = "\"http://localhost:8080/api\"";
      ServersideAbsoluteUrl result = objectMapper.readValue(json, ServersideAbsoluteUrl.class);
      assertThat(result.toString()).isEqualTo("http://localhost:8080/api");
    }

    @Test
    void deserializesUri() throws JsonProcessingException {
      String json = "\"https://example.com/path\"";
      Uri result = objectMapper.readValue(json, Uri.class);
      assertThat(result.toString()).isEqualTo("https://example.com/path");
    }

    @Test
    void deserializesUrl() throws JsonProcessingException {
      String json = "\"/path/to/resource\"";
      Url result = objectMapper.readValue(json, Url.class);
      assertThat(result.toString()).isEqualTo("/path/to/resource");
    }

    @Test
    void deserializesUrlWithAuthority() throws JsonProcessingException {
      String json = "\"//example.com/path\"";
      UrlWithAuthority result = objectMapper.readValue(json, UrlWithAuthority.class);
      assertThat(result.toString()).isEqualTo("//example.com/path");
      assertThat(result.getAuthority().getHost().toString()).isEqualTo("example.com");
    }

    @Test
    void deserializesUserInfo() throws JsonProcessingException {
      String json = "\"user:password\"";
      UserInfo result = objectMapper.readValue(json, UserInfo.class);
      assertThat(result.toString()).isEqualTo("user:password");
    }

    @Test
    void deserializesUsername() throws JsonProcessingException {
      String json = "\"johndoe\"";
      Username result = objectMapper.readValue(json, Username.class);
      assertThat(result.toString()).isEqualTo("johndoe");
    }

    @Test
    void deserializesNullAsNull() throws JsonProcessingException {
      String json = "null";
      Host result = objectMapper.readValue(json, Host.class);
      assertThat(result).isNull();
    }
  }

  @Nested
  class Serialization {

    @Test
    void serializesHost() throws JsonProcessingException {
      Host host = Host.parse("example.com");
      String json = objectMapper.writeValueAsString(host);
      assertThat(json).isEqualTo("\"example.com\"");
    }

    @Test
    void serializesAbsoluteUrl() throws JsonProcessingException {
      AbsoluteUrl url = AbsoluteUrl.parse("https://example.com/path");
      String json = objectMapper.writeValueAsString(url);
      assertThat(json).isEqualTo("\"https://example.com/path\"");
    }

    @Test
    void serializesAuthority() throws JsonProcessingException {
      Authority authority = Authority.parse("user@example.com:8080");
      String json = objectMapper.writeValueAsString(authority);
      assertThat(json).isEqualTo("\"user@example.com:8080\"");
    }

    @Test
    void serializesPath() throws JsonProcessingException {
      Path path = Path.parse("/api/v1/users");
      String json = objectMapper.writeValueAsString(path);
      assertThat(json).isEqualTo("\"/api/v1/users\"");
    }

    @Test
    void serializesQuery() throws JsonProcessingException {
      Query query = Query.parse("key=value&foo=bar");
      String json = objectMapper.writeValueAsString(query);
      assertThat(json).isEqualTo("\"key=value&foo=bar\"");
    }

    @Test
    void serializesPort() throws JsonProcessingException {
      Port port = Port.parse("443");
      String json = objectMapper.writeValueAsString(port);
      assertThat(json).isEqualTo("\"443\"");
    }

    @Test
    void serializesScheme() throws JsonProcessingException {
      Scheme scheme = Scheme.parse("https");
      String json = objectMapper.writeValueAsString(scheme);
      assertThat(json).isEqualTo("\"https\"");
    }

    @Test
    void serializesNullAsNull() throws JsonProcessingException {
      Host host = null;
      String json = objectMapper.writeValueAsString(host);
      assertThat(json).isEqualTo("null");
    }
  }

  @Nested
  class RoundTrip {

    static Stream<Arguments> roundTripTestCases() {
      return Stream.of(
          Arguments.of(AbsoluteUri.class, "https://example.com/path?q=1#frag"),
          Arguments.of(AbsoluteUrl.class, "https://user:pass@example.com:8080/path"),
          Arguments.of(Authority.class, "user:pass@example.com:8080"),
          Arguments.of(Fragment.class, "section-1"),
          Arguments.of(Host.class, "example.com"),
          Arguments.of(Host.class, "[::1]"),
          Arguments.of(HostAndPort.class, "example.com:443"),
          Arguments.of(OpaqueUri.class, "mailto:test@example.com"),
          Arguments.of(Origin.class, "https://example.com"),
          Arguments.of(Password.class, "secret%20pass"),
          Arguments.of(Path.class, "/api/v1/users"),
          Arguments.of(Path.class, "/path%20with%20spaces"),
          Arguments.of(PathAndQuery.class, "/path?key=value"),
          Arguments.of(Port.class, "8080"),
          Arguments.of(Query.class, "a=1&b=2&c=3"),
          Arguments.of(QueryParamKey.class, "param-name"),
          Arguments.of(QueryParamValue.class, "param-value"),
          Arguments.of(RelativeUrl.class, "/relative/path?q=test"),
          Arguments.of(Scheme.class, "https"),
          Arguments.of(Scheme.class, "custom-scheme"),
          Arguments.of(SchemeRelativeUrl.class, "//example.com/path"),
          Arguments.of(Segment.class, "path-segment"),
          Arguments.of(ServersideAbsoluteUrl.class, "http://localhost:8080/api"),
          Arguments.of(Uri.class, "https://example.com/path"),
          Arguments.of(Url.class, "/some/path"),
          Arguments.of(UrlWithAuthority.class, "https://example.com/path"),
          Arguments.of(UserInfo.class, "user:password"),
          Arguments.of(Username.class, "john-doe"));
    }

    @ParameterizedTest
    @MethodSource("roundTripTestCases")
    void roundTripPreservesValue(Class<?> type, String value) throws JsonProcessingException {
      String json = "\"" + value + "\"";

      // Deserialize
      Object deserialized = objectMapper.readValue(json, type);
      assertThat(deserialized.toString()).isEqualTo(value);

      // Serialize back
      String serialized = objectMapper.writeValueAsString(deserialized);
      assertThat(serialized).isEqualTo(json);

      // Deserialize again
      Object deserializedAgain = objectMapper.readValue(serialized, type);
      assertThat(deserializedAgain.toString()).isEqualTo(value);
    }
  }

  @Nested
  class PercentEncoding {

    @Test
    void preservesPercentEncodingInPath() throws JsonProcessingException {
      String json = "\"/path%20with%20spaces\"";
      Path result = objectMapper.readValue(json, Path.class);
      assertThat(result.toString()).isEqualTo("/path%20with%20spaces");
      assertThat(result.decode()).isEqualTo("/path with spaces");
    }

    @Test
    void preservesPercentEncodingInQuery() throws JsonProcessingException {
      String json = "\"key%3Dname=value%26more\"";
      Query result = objectMapper.readValue(json, Query.class);
      assertThat(result.toString()).isEqualTo("key%3Dname=value%26more");
    }

    @Test
    void preservesPercentEncodingInHost() throws JsonProcessingException {
      String json = "\"xn--n3h.com\"";
      Host result = objectMapper.readValue(json, Host.class);
      assertThat(result.toString()).isEqualTo("xn--n3h.com");
    }
  }

  @Nested
  class ErrorHandling {

    @Test
    void throwsOnInvalidPort() {
      String json = "\"not-a-number\"";
      assertThatThrownBy(() -> objectMapper.readValue(json, Port.class))
          .isInstanceOf(JsonMappingException.class);
    }

    @Test
    void throwsOnInvalidScheme() {
      String json = "\"123invalid\"";
      assertThatThrownBy(() -> objectMapper.readValue(json, Scheme.class))
          .isInstanceOf(JsonMappingException.class);
    }

    @Test
    void throwsOnInvalidAbsoluteUrl() {
      String json = "\"/relative/path\"";
      assertThatThrownBy(() -> objectMapper.readValue(json, AbsoluteUrl.class))
          .isInstanceOf(JsonMappingException.class);
    }

    @Test
    void throwsOnInvalidRelativeUrl() {
      String json = "\"https://example.com/absolute\"";
      assertThatThrownBy(() -> objectMapper.readValue(json, RelativeUrl.class))
          .isInstanceOf(JsonMappingException.class);
    }

    @Test
    void throwsOnInvalidOpaqueUri() {
      String json = "\"https://example.com/not-opaque\"";
      assertThatThrownBy(() -> objectMapper.readValue(json, OpaqueUri.class))
          .isInstanceOf(JsonMappingException.class);
    }
  }

  @Nested
  class ObjectFieldDeserialization {

    @Test
    void deserializesObjectWithUrlField() throws JsonProcessingException {
      String json = "{\"url\": \"https://example.com/api\"}";
      TestRecord result = objectMapper.readValue(json, TestRecord.class);
      assertThat(result.url().toString()).isEqualTo("https://example.com/api");
    }

    @Test
    void serializesObjectWithUrlField() throws JsonProcessingException {
      TestRecord record = new TestRecord(AbsoluteUrl.parse("https://example.com/api"));
      String json = objectMapper.writeValueAsString(record);
      assertThat(json).isEqualTo("{\"url\":\"https://example.com/api\"}");
    }

    record TestRecord(AbsoluteUrl url) {}
  }
}
