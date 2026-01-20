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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.wiremock.url.Scheme.file;
import static org.wiremock.url.Scheme.wss;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class UrlTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = Url.parse("https://example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_absolute_url_with_userinfo_correctly() {
      var absoluteUrl = Url.parse("https://user@example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://user@example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = Url.parse("https://example.com/path?query");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("https://example.com/path?query");
      assertThat(serversideAbsoluteUrl).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(serversideAbsoluteUrl).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_origin_correctly() {
      var origin = Url.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);
    }

    @Test
    void parses_relative_url_with_authority_correctly() {
      var relativeUrl = Url.parse("//example.com/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("//example.com/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_relative_url_without_authority_correctly() {
      var relativeUrl = Url.parse("/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_path_and_query_correctly() {
      var pathAndQuery = Url.parse("/path?query");

      assertThat(pathAndQuery.toString()).isEqualTo("/path?query");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_relative_path_correctly() {
      var pathAndQuery = Url.parse("relative");

      assertThat(pathAndQuery.toString()).isEqualTo("relative");
      assertThat(pathAndQuery).isInstanceOf(RelativeUrl.class);
    }

    @Test
    void parses_empty_path_correctly() {
      var pathAndQuery = Url.parse("");

      assertThat(pathAndQuery.toString()).isEqualTo("");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_query_only_correctly() {
      var pathAndQuery = Url.parse("?");

      assertThat(pathAndQuery.toString()).isEqualTo("?");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_fragment_only_correctly() {
      var relativeUrl = Url.parse("#");

      assertThat(relativeUrl.toString()).isEqualTo("#");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = Url.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = Url.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Url.parse("not a :uri"))
              .actual();
      assertThat(exception.getMessage()).isEqualTo("Illegal uri: `not a :uri`");
      assertThat(exception.getIllegalValue()).isEqualTo("not a :uri");

      IllegalScheme cause =
          assertThat(exception.getCause()).asInstanceOf(type(IllegalScheme.class)).actual();
      assertThat(cause.getMessage())
          .isEqualTo("Illegal scheme `not a `; Scheme must match [a-zA-Z][a-zA-Z0-9+\\-.]{0,255}");
      assertThat(cause.getIllegalValue()).isEqualTo("not a ");
      assertThat(cause.getCause()).isNull();
    }

    static final List<String> illegalUrls =
        Stream.of(
                "mailto:joan@example.com",
                "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS",
                "file:/home/me/some/dir")
            .toList();

    @ParameterizedTest
    @FieldSource("illegalUrls")
    void rejects_illegal_url(String illegalUrl) {
      assertThatExceptionOfType(IllegalUrl.class)
          .isThrownBy(() -> Url.parse(illegalUrl))
          .withMessage("Illegal url: `" + illegalUrl + "`; a url has an authority")
          .extracting(IllegalUrl::getIllegalValue)
          .isEqualTo(illegalUrl);
    }
  }

  @Nested
  class Transform {

    @Test
    void can_change_an_absolute_urls_scheme() {

      Url url = Url.parse("https://user@example.com:8443/path?query#fragment");
      Url transformed = url.transform(it -> it.setScheme(wss));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(Url.parse("wss://user@example.com:8443/path?query#fragment"));
    }

    @Test
    void can_set_a_scheme_relative_urls_scheme() {

      Url url = Url.parse("//user@example.com:8443/path?query#fragment");
      Url transformed = url.transform(it -> it.setScheme(wss));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(Url.parse("wss://user@example.com:8443/path?query#fragment"));
    }

    @Test
    void can_set_a_relative_urls_scheme_with_authority() {

      Url url = Url.parse("/path?query#fragment");
      Url transformed =
          url.transform(it -> it.setScheme(wss).setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(Url.parse("wss://www.example.com/path?query#fragment"));
    }

    @Test
    void cannot_set_a_relative_urls_scheme_without_authority() {

      Url url = Url.parse("/path?query#fragment");
      assertThatExceptionOfType(IllegalUrl.class)
          .isThrownBy(() -> url.transform(it -> it.setScheme(file)))
          .withMessage("Illegal url: `file:/path?query#fragment`; a url has an authority")
          .withNoCause()
          .extracting(IllegalUrl::getIllegalValue)
          .isEqualTo("file:/path?query#fragment");
    }

    @Test
    void can_change_an_absolute_urls_authority() {

      Url url = Url.parse("https://user@example.com:8443/path?query#fragment");
      Url transformed = url.transform(it -> it.setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(Url.parse("https://www.example.com/path?query#fragment"));
    }

    @Test
    void can_change_a_scheme_relative_urls_authority() {

      Url url = Url.parse("//user@example.com:8443/path?query#fragment");
      Url transformed = url.transform(it -> it.setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(SchemeRelativeUrl.class)
          .isEqualTo(Url.parse("//www.example.com/path?query#fragment"));
    }

    @Test
    void can_set_a_relative_urls_authority() {

      Url url = Url.parse("/path?query#fragment");
      Url transformed = url.transform(it -> it.setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(SchemeRelativeUrl.class)
          .isEqualTo(Url.parse("//www.example.com/path?query#fragment"));
    }
  }

  @Nested
  class Builder {

    @Test
    void can_update_query() {
      var url = Url.parse("https://example.com/?a=b");
      Url.Transformer<?> builder = url.thaw();
      builder.getQuery().append("b", "2");
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/?a=b&b=2");
    }

    @Test
    void getQuery_returns_empty_builder_when_no_query() {
      var url = Url.parse("https://example.com/");
      Url.Transformer<?> builder = url.thaw();
      Query.Builder queryBuilder = builder.getQuery();
      assertThat(queryBuilder.build().toString()).isEmpty();
    }

    @Test
    void getQuery_returns_builder_with_existing_params() {
      var url = Url.parse("https://example.com/?a=1&b=2");
      Url.Transformer<?> builder = url.thaw();
      Query.Builder queryBuilder = builder.getQuery();
      Query built = queryBuilder.build();
      assertThat(built.get("a")).containsExactly(QueryParamValue.parse("1"));
      assertThat(built.get("b")).containsExactly(QueryParamValue.parse("2"));
    }

    @Test
    void getQuery_returns_same_builder_on_multiple_calls() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      Query.Builder queryBuilder1 = builder.getQuery();
      Query.Builder queryBuilder2 = builder.getQuery();
      assertThat(queryBuilder1).isSameAs(queryBuilder2);
    }

    @Test
    void getQuery_modifications_reflected_in_build() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      builder.getQuery().append("b", "2");
      builder.getQuery().append("c", "3");
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/?a=1&b=2&c=3");
    }

    @Test
    void setQuery_clears_query_builder() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      builder.getQuery().append("b", "2");
      builder.setQuery(Query.parse("x=9"));
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/?x=9");
    }

    @Test
    void setQuery_with_builder_sets_query() {
      var url = Url.parse("https://example.com/");
      Url.Transformer<?> builder = url.thaw();
      Query.Builder queryBuilder = Query.builder().append("a", "1").append("b", "2");
      builder.setQuery(queryBuilder);
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/?a=1&b=2");
    }

    @Test
    void setQuery_with_empty_builder_removes_query() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      builder.setQuery(Query.builder());
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/");
    }

    @Test
    void setQuery_null_removes_query() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      builder.setQuery((Query) null);
      Url updated = builder.build();
      assertThat(updated).hasToString("https://example.com/");
    }

    @Test
    void getQuery_after_setQuery_returns_new_builder() {
      var url = Url.parse("https://example.com/?a=1");
      Url.Transformer<?> builder = url.thaw();
      Query.Builder originalBuilder = builder.getQuery();
      builder.setQuery(Query.parse("x=9"));
      Query.Builder newBuilder = builder.getQuery();
      assertThat(newBuilder).isNotSameAs(originalBuilder);
      assertThat(newBuilder.build().get("x")).containsExactly(QueryParamValue.parse("9"));
    }

    @Test
    void can_chain_getQuery_modifications() {
      var url = Url.parse("https://example.com/");
      Url updated =
          url.transform(
              builder -> {
                builder.getQuery().append("a", "1");
                builder.getQuery().remove("a");
                builder.getQuery().append("b", "2");
              });
      assertThat(updated).hasToString("https://example.com/?b=2");
    }

    @Test
    void getQuery_with_put_replaces_values() {
      var url = Url.parse("https://example.com/?a=1&a=2");
      Url updated =
          url.transform(
              builder -> {
                builder.getQuery().put("a", "replaced");
              });
      assertThat(updated).hasToString("https://example.com/?a=replaced");
    }
  }
}
