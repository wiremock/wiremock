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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.wiremock.url.PercentEncodedStringParserInvariantTests.generateEncodeDecodeInvariantTests;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class QueryTests {

  @Nested
  class Parse {

    static final List<String> validQueries =
        List.of(
            // Empty and simple queries
            "",
            "q=search",
            "key=value",
            "name=John",
            "id=123",

            // Multiple parameters
            "q=search&page=1",
            "name=John&age=30&city=NYC",
            "a=1&b=2&c=3",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "query-name=value",
            "query.name=value",
            "query_name=value",
            "query~name=value",
            "Query123=Value456",
            "test-param_123.name~test=result",

            // Sub-delimiters (!$&'()*+,;=)
            "query!name=value",
            "query$name=value",
            "key&key2=value",
            "query'name=value",
            "query(name)=value",
            "query*name=value",
            "query+name=value",
            "query,name=value",
            "query;name=value",
            "key=val=ue",

            // Colon and at-sign
            "time=12:30:00",
            "email=user@example.com",
            "url=http://example.com",
            "path=/api/v1/users",

            // Forward slash and question mark
            "path=/path/to/resource",
            "query=what?when?where",
            "url=/search?q=test",
            "nested=a=b?c=d",

            // Percent-encoded characters
            "%20=value", // space key
            "query=%20", // space value
            "q=search%20term", // search term
            "path=%2Fapi%2Fv1", // /api/v1
            "name=%C3%A9ric", // éric
            "caf%C3%A9=coffee", // café=coffee
            "percent=%25", // %

            // Empty values
            "key=",
            "key1=&key2=",
            "=value",
            "=",

            // Characters that extend beyond RFC 3986
            "key={value}",
            "data=[1,2,3]",
            "path=<value>",
            "pipe=val|ue",
            "back=val\\ue",
            "caret=val^ue",
            "grave=val`ue",

            // Spaces and special characters (permissive)
            "q=search term", // unencoded space
            "na[me]=value", // brackets
            "key=<value>", // angle brackets

            // Complex combinations
            "q=test&page=1&limit=10",
            "filter=name:John&sort=date:desc",
            "url=http://example.com:8080/path?q=test",
            "data=%7B%22key%22:%22value%22%7D", // {"key":"value"}
            "callback=jQuery.ajax&_=1234567890",

            // No separators
            "justtext",
            "noseparators123",

            // Edge cases
            "?nested=question",
            "key=value&&&",
            "===",
            "&&&",

            // Invalid percent encoding (still accepted - permissive parser)
            "q=%", // incomplete
            "key=%2", // incomplete
            "val=%GG", // invalid hex
            "query=%ZZvalue"); // invalid hex

    @ParameterizedTest
    @FieldSource("validQueries")
    void parses_valid_queries(String queryString) {
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @Test
    void get_entries_returns_flat_key_value_pairs() {
      Query parsed = Query.parse("x=1&y&x&y=2&z");
      assertThat(parsed.getEntries())
          .isEqualTo(
              List.of(
                  flatEntry("x", "1"),
                  flatEntry("y", null),
                  flatEntry("x", null),
                  flatEntry("y", "2"),
                  flatEntry("z", null)));
    }

    @Test
    void get_entry_set_returns_grouped_entries() {
      Query parsed = Query.parse("x=1&y&x&y=2&z");
      assertThat(parsed.asMap())
          .isEqualTo(Map.ofEntries(entry("x", "1", null), entry("y", null, "2"), entry("z")));
    }

    @Test
    void get_values_by_string_key() {
      Query parsed = Query.parse("x=1&y&x&y=2&z");
      assertThat(parsed.get("x")).isEqualTo(values("1", null));
      assertThat(parsed.getFirst("x")).isEqualTo(QueryParamValue.parse("1"));
      assertThat(parsed.getFirst("y")).isEqualTo(QueryParamValue.EMPTY);
      assertThat(parsed.getFirst("not_present")).isNull();
    }

    @Test
    void get_values_by_query_param_key() {
      Query parsed = Query.parse("x=1&y&x&y=2&z");
      assertThat(parsed.get(QueryParamKey.parse("x"))).isEqualTo(values("1", null));
      assertThat(parsed.getFirst(QueryParamKey.parse("x"))).isEqualTo(QueryParamValue.parse("1"));
      assertThat(parsed.getFirst(QueryParamKey.parse("y"))).isEqualTo(QueryParamValue.EMPTY);
      assertThat(parsed.getFirst(QueryParamKey.parse("not_present"))).isNull();
    }

    private static final List<Pair<Query, QueryParamKey>> normalisedKeyTestCases =
        Stream.of(
                Pair.of("a b=c", "a b"),
                Pair.of("a b=c", "a%20b"),
                Pair.of("a b=c", "a+b"),
                Pair.of("a%20b=c", "a b"),
                Pair.of("a%20b=c", "a%20b"),
                Pair.of("a%20b=c", "a+b"),
                Pair.of("a+b=c", "a b"),
                Pair.of("a+b=c", "a%20b"),
                Pair.of("a+b=c", "a+b"))
            .map(
                testCase ->
                    Pair.of(
                        Query.parse(testCase.getLeft()), QueryParamKey.parse(testCase.getRight())))
            .toList();

    @ParameterizedTest
    @FieldSource("normalisedKeyTestCases")
    void can_get_with_normalised_key(Pair<Query, QueryParamKey> testCase) {
      var query = testCase.getLeft();
      var key = testCase.getRight();
      assertThat(query.get(key)).isEqualTo(List.of(QueryParamValue.parse("c")));
    }

    @ParameterizedTest
    @FieldSource("normalisedKeyTestCases")
    void can_get_first_with_normalised_key(Pair<Query, QueryParamKey> testCase) {
      var query = testCase.getLeft();
      var key = testCase.getRight();
      assertThat(query.getFirst(key)).isEqualTo(QueryParamValue.parse("c"));
    }

    @Test
    void get_keys_returns_all_keys() {
      Query parsed = Query.parse("x=1&y&x&y=2&z");
      assertThat(parsed.getKeys())
          .isEqualTo(
              Set.of(QueryParamKey.parse("x"), QueryParamKey.parse("y"), QueryParamKey.parse("z")));
    }

    @Test
    void get_first_with_percent_encoded_key_returns_value() {
      Query parsed = Query.parse("a&b%3D1%262=c%3D2%263=4&");
      assertThat(parsed.getFirst("a")).hasToString("");
      assertThat(parsed.getFirst("b=1&2")).hasToString("c%3D2%263=4");
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=val#ue", "query#fragment", "#", "test#test"})
    void rejects_queries_with_hash(String illegalQuery) {
      assertThatExceptionOfType(IllegalQuery.class)
          .isThrownBy(() -> Query.parse(illegalQuery))
          .withMessage("Illegal query: `" + illegalQuery + "`")
          .extracting(IllegalQuery::getIllegalValue)
          .isEqualTo(illegalQuery);
    }

    @Test
    void get_empty_works() {
      Query parsed = Query.parse("");
      assertThat(parsed.getEntries()).isEqualTo(List.of());
      assertThat(parsed.get("")).isEqualTo(List.of());
      assertThat(parsed.getFirst("")).isEqualTo(null);
      assertThat(parsed.getKeys()).isEqualTo(Set.of());
    }

    @Test
    void get_multiple_empty_works() {
      Query parsed = Query.parse("&");
      assertThat(parsed.getEntries()).isEqualTo(List.of(flatEntry("", null), flatEntry("", null)));
      assertThat(parsed.get("")).isEqualTo(values(null, null));
      assertThat(parsed.getFirst("")).isEqualTo(QueryParamValue.EMPTY);
      assertThat(parsed.getKeys()).isEqualTo(Set.of(QueryParamKey.parse("")));
      assertThat(parsed.contains("")).isTrue();
    }

    @Test
    void plus_in_query_decoded_in_query_param_value() {
      var query = Query.parse("a+2=b+1");
      assertThat(query).hasToString("a+2=b+1");
      assertThat(query.decode()).isEqualTo("a+2=b+1");

      var value = query.getFirst("a 2");
      assertThat(value).hasToString("b+1").extracting(QueryParamValue::decode).isEqualTo("b 1");
    }

    @Test
    void as_decoded_map_returns_decoded_keys_and_values() {
      var query = Query.parse("a%20b=c%20d&e+f=g+h&plain=value");
      var decoded = query.asDecodedMap();
      assertThat(decoded)
          .containsEntry("a b", List.of("c d"))
          .containsEntry("e f", List.of("g h"))
          .containsEntry("plain", List.of("value"));
    }

    @Test
    void as_decoded_map_groups_multiple_values() {
      var query = Query.parse("key=first&key=second&key=third");
      var decoded = query.asDecodedMap();
      assertThat(decoded).containsEntry("key", List.of("first", "second", "third"));
    }

    @Test
    void as_decoded_map_returns_empty_string_for_null_values() {
      var query = Query.parse("key1&key2=value&key1=second");
      var decoded = query.asDecodedMap();
      assertThat(decoded)
          .containsEntry("key1", List.of("", "second"))
          .containsEntry("key2", List.of("value"));
    }

    @Test
    void can_get_first_decoded() {
      var query = Query.parse("a+b=c");
      Assertions.assertThat(query.getFirstDecoded("a b")).isEqualTo("c");
    }

    @Test
    void get_first_decoded_returns_empty_string_for_key_without_value() {
      var query = Query.parse("a&a=ignored");
      Assertions.assertThat(query.getFirstDecoded("a")).isEqualTo("");
    }

    @Test
    void get_decoded_returns_empty_strings_for_keys_without_value() {
      var query = Query.parse("a&a=second&a=");
      Assertions.assertThat(query.getDecoded("a")).isEqualTo(List.of("", "second", ""));
    }

    @Test
    void get_decoded_returns_empty_list_for_missing_key() {
      var query = Query.parse("a&a=second&a=");
      Assertions.assertThat(query.getDecoded("b")).isEqualTo(List.of());
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          QueryParser.INSTANCE, Parse.validQueries);
    }

    private static Map.Entry<QueryParamKey, @Nullable QueryParamValue> flatEntry(
        String key, @Nullable String value) {
      return new SimpleEntry<>(QueryParamKey.parse(key), parseOrNull(value));
    }

    private static Map.Entry<QueryParamKey, List<@Nullable QueryParamValue>> entry(String key) {
      return entry(key, (String) null);
    }

    private static Map.Entry<QueryParamKey, List<@Nullable QueryParamValue>> entry(
        String x, @Nullable String... values) {
      return new SimpleEntry<>(QueryParamKey.parse(x), values(values));
    }

    private static @Nullable QueryParamValue parseOrNull(@Nullable String value) {
      return value != null ? QueryParamValue.parse(value) : null;
    }

    private static List<@Nullable QueryParamValue> values(@Nullable String... values) {
      return Arrays.stream(values).map(Parse::parseOrNull).toList();
    }
  }

  @Nested
  class Normalise {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "a b", "a+b", "a%20b",
        })
    void query_param_key_normalises(String input) {
      var key = QueryParamKey.parse(input);
      assertThat(key.normalise()).hasToString("a%20b");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "a b", "a+b", "a%20b",
        })
    void query_param_value_normalises(String input) {
      var value = QueryParamValue.parse(input);
      assertThat(value.normalise()).hasToString("a%20b");
    }

    record NormalisationCase(String input, String expected) {}

    static final List<NormalisationCase> normalisationCases =
        List.of(
            new NormalisationCase("q=search term", "q=search%20term"),
            new NormalisationCase("key=value test", "key=value%20test"),
            new NormalisationCase("q=test\"quote", "q=test%22quote"),
            new NormalisationCase("data={value}", "data=%7Bvalue%7D"),
            new NormalisationCase("q=test<tag>", "q=test%3Ctag%3E"),
            new NormalisationCase("name=café", "name=caf%C3%A9"),
            new NormalisationCase("%ff", "%FF"),
            new NormalisationCase("%fF", "%FF"),
            new NormalisationCase("%Ff", "%FF"),
            new NormalisationCase("key=}value{", "key=%7Dvalue%7B"));

    static final List<String> alreadyNormalisedQueries =
        List.of(
            "",
            "q=search",
            "key=value",
            "a=1&b=2",
            "query-name=value",
            "time=12:30:00",
            "path=/api/v1",
            "q=search%20term",
            "name=%C3%A9ric",
            "q=test'quote",
            "%41",
            "a&b%3D1%262=c%3D2%263=4&");

    @TestFactory
    Stream<DynamicTest> normalises_query_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream()
              .map(
                  testCase ->
                      new NormalisableInvariantTests.NormalisationCase<>(
                          Query.parse(testCase.input()), Query.parse(testCase.expected())))
              .toList());
    }

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedQueries.stream().map(Query::parse).toList());
    }
  }

  @Nested
  class Codec {

    @Test
    void encode_double_encodes_percent_signs() {
      Query encoded = Query.encode("a&b%3D1%262=c%3D2%263=4&");
      assertThat(encoded.toString()).isEqualTo("a&b%253D1%25262=c%253D2%25263=4&");
    }

    static final List<String> queriesWithoutPercentEncoding =
        List.of("", "q=search", "key=value", "a=1&b=2", "time=12:30:00", "path=/api/v1");

    static final List<CodecCase> decodeCases =
        List.of(
            new CodecCase("q=search%20term", "q=search term"),
            new CodecCase("name=%C3%A9ric", "name=éric"),
            new CodecCase("caf%C3%A9=coffee", "café=coffee"),
            new CodecCase("percent=%25", "percent=%"),
            new CodecCase("path=%2Fapi%2Fv1", "path=/api/v1"),
            new CodecCase("data=%7B%22key%22:%22value%22%7D", "data={\"key\":\"value\"}"),
            new CodecCase("query=%20", "query= "),
            new CodecCase("%20=value", " =value"),
            new CodecCase("a&b%253D1%25262=c%253D2%25263=4&", "a&b%3D1%262=c%3D2%263=4&"),
            new CodecCase("q=hello%20world%21", "q=hello world!"));

    @ParameterizedTest
    @FieldSource("queriesWithoutPercentEncoding")
    void returns_same_string_for_query_without_percent_encoding(String queryString) {
      Query query = Query.parse(queryString);
      assertThat(query.decode()).isEqualTo(queryString);
    }

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_percent_encoded_query_correctly(CodecCase testCase) {
      Query query = Query.parse(testCase.encoded());
      assertThat(query.decode()).isEqualTo(testCase.decoded());
    }

    @Test
    void encode_does_not_encode_pluses() {
      Query query = Query.encode("a+b");
      assertThat(query).hasToString("a+b");
      assertThat(query.decode()).isEqualTo("a+b");
    }

    @Test
    void encode_encodes_spaces_as_percent20() {
      Query query = Query.encode("a b");
      assertThat(query).hasToString("a%20b");
      assertThat(query.decode()).isEqualTo("a b");
    }

    @Test
    void query_param_key_encode_encodes_pluses() {
      var key = QueryParamKey.encode("a+b");
      assertThat(key).hasToString("a%2Bb");
      assertThat(key.decode()).isEqualTo("a+b");
    }

    @Test
    void query_param_key_encode_encodes_spaces_as_percent20() {
      var key = QueryParamKey.encode("a b");
      assertThat(key).hasToString("a%20b");
      assertThat(key.decode()).isEqualTo("a b");
    }

    @Test
    void query_param_key_decode_decodes_pluses_as_spaces() {
      var key = QueryParamKey.parse("a+b");
      assertThat(key).hasToString("a+b");
      assertThat(key.decode()).isEqualTo("a b");
    }

    @Test
    void query_param_value_encode_encodes_pluses() {
      var value = QueryParamValue.encode("a+b");
      assertThat(value).hasToString("a%2Bb");
      assertThat(value.decode()).isEqualTo("a+b");
    }

    @Test
    void query_param_value_encode_encodes_spaces_as_percent20() {
      var value = QueryParamValue.encode("a b");
      assertThat(value).hasToString("a%20b");
      assertThat(value.decode()).isEqualTo("a b");
    }

    @Test
    void query_param_value_decode_decodes_pluses_as_spaces() {
      var value = QueryParamValue.parse("a+b");
      assertThat(value).hasToString("a+b");
      assertThat(value.decode()).isEqualTo("a b");
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      return generateEncodeDecodeInvariantTests(
          QueryParser.INSTANCE,
          Stream.of(
              "foo",
              "bar",
              "key=value",
              "hello world",
              "q=café",
              "param1=value1&param2=value2",
              "a&b%3D1%262=c%3D2%263=4&",
              "こんにちは"));
    }
  }

  @Nested
  class Equality {

    @Test
    void queries_with_same_value_are_equal() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("q=search");
      assertThat(query1).isEqualTo(query2);
    }

    @Test
    void queries_with_different_values_are_not_equal() {
      Query query1 = Query.parse("q=search1");
      Query query2 = Query.parse("q=search2");
      assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void queries_with_different_case_are_not_equal() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("Q=SEARCH");
      assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void query_is_equal_to_itself() {
      Query query = Query.parse("q=search");
      assertThat(query).isEqualTo(query);
    }

    @Test
    void query_is_not_equal_to_null() {
      Query query = Query.parse("q=search");
      assertThat(query).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void query_is_not_equal_to_different_type() {
      Query query = Query.parse("q=search");
      assertThat(query).isNotEqualTo("q=search");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_queries_have_same_hash_code() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("q=search");
      assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Query query = Query.parse("q=search&page=1");
      int hashCode1 = query.hashCode();
      int hashCode2 = query.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_query() {
      String queryString = "q=search&page=1";
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @Test
    void to_string_preserves_case() {
      String queryString = "Query=Search";
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "q=search%20term";
      Query query = Query.parse(encoded);
      assertThat(query.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Query original = Query.parse("q=test&page=1&limit=10");
      String stringForm = original.toString();
      Query parsed = Query.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @Nested
  class Builder {

    @Test
    void builds_empty_query() {
      Query query = Query.builder().build();
      assertThat(query.toString()).isEmpty();
      assertThat(query.getKeys()).isEqualTo(Set.of());
    }

    @Test
    void appends_single_key_value_pair_with_strings() {
      Query query = Query.builder().append("key", "value").build();
      assertThat(query.toString()).isEqualTo("key=value");
    }

    @Test
    void appends_single_key_value_pair_with_typed_params() {
      Query query =
          Query.builder()
              .append(QueryParamKey.encode("key"), QueryParamValue.encode("value"))
              .build();
      assertThat(query.toString()).isEqualTo("key=value");
    }

    @Test
    void appends_key_with_null_value() {
      Query query = Query.builder().append("key", null).build();
      assertThat(query.toString()).isEqualTo("key");
      assertThat(query.getFirst("key")).hasToString("");
    }

    @Test
    void appends_multiple_key_value_pairs() {
      Query query = Query.builder().append("a", "1").append("b", "2").append("c", "3").build();
      assertThat(query.toString()).isEqualTo("a=1&b=2&c=3");
    }

    @Test
    void appends_multiple_values_for_same_key() {
      Query query = Query.builder().append("key", "value1").append("key", "value2").build();
      assertThat(query.toString()).isEqualTo("key=value1&key=value2");
      assertThat(query.get("key"))
          .containsExactly(QueryParamValue.parse("value1"), QueryParamValue.parse("value2"));
    }

    @Test
    void appends_multiple_values_at_once() {
      Query query = Query.builder().append("key", "value1", "value2", "value3").build();
      assertThat(query.toString()).isEqualTo("key=value1&key=value2&key=value3");
    }

    @Test
    void appends_values_with_list() {
      Query query =
          Query.builder()
              .append(
                  QueryParamKey.encode("key"),
                  List.of(QueryParamValue.encode("a"), QueryParamValue.encode("b")))
              .build();
      assertThat(query.toString()).isEqualTo("key=a&key=b");
    }

    @Test
    void put_replaces_existing_values() {
      Query query =
          Query.builder().append("key", "old1").append("key", "old2").put("key", "new").build();
      assertThat(query.toString()).isEqualTo("key=new");
      assertThat(query.get("key")).containsExactly(QueryParamValue.parse("new"));
    }

    @Test
    void put_adds_if_key_not_present() {
      Query query = Query.builder().append("other", "value").put("key", "new").build();
      assertThat(query.toString()).isEqualTo("other=value&key=new");
    }

    @Test
    void put_with_multiple_values() {
      Query query = Query.builder().append("key", "old").put("key", "new1", "new2").build();
      assertThat(query.toString()).isEqualTo("key=new1&key=new2");
    }

    @Test
    void put_with_typed_params() {
      Query query =
          Query.builder()
              .append("key", "old")
              .put(QueryParamKey.encode("key"), QueryParamValue.encode("new"))
              .build();
      assertThat(query.toString()).isEqualTo("key=new");
    }

    @Test
    void remove_by_key_string() {
      Query query = Query.builder().append("a", "1").append("b", "2").remove("a").build();
      assertThat(query.toString()).isEqualTo("b=2");
      assertThat(query.contains("a")).isFalse();
    }

    @Test
    void remove_by_typed_key() {
      Query query =
          Query.builder()
              .append("a", "1")
              .append("b", "2")
              .remove(QueryParamKey.encode("a"))
              .build();
      assertThat(query.toString()).isEqualTo("b=2");
    }

    @Test
    void remove_all_values_for_key() {
      Query query =
          Query.builder()
              .append("key", "value1")
              .append("key", "value2")
              .append("other", "x")
              .remove("key")
              .build();
      assertThat(query.toString()).isEqualTo("other=x");
    }

    @Test
    void remove_specific_value_from_key() {
      Query query =
          Query.builder()
              .append("key", "keep")
              .append("key", "remove")
              .append("key", "also-keep")
              .remove(QueryParamKey.encode("key"), List.of(QueryParamValue.encode("remove")))
              .build();
      assertThat(query.toString()).isEqualTo("key=keep&key=also-keep");
    }

    @Test
    void remove_multiple_specific_values() {
      Query query =
          Query.builder()
              .append("key", "a")
              .append("key", "b")
              .append("key", "c")
              .append("key", "d")
              .remove(
                  QueryParamKey.encode("key"),
                  List.of(QueryParamValue.encode("b"), QueryParamValue.encode("d")))
              .build();
      assertThat(query.toString()).isEqualTo("key=a&key=c");
    }

    @Test
    void remove_with_typed_key_and_value_list() {
      Query query =
          Query.builder()
              .append("key", "keep")
              .append("key", "remove")
              .remove(QueryParamKey.encode("key"), List.of(QueryParamValue.encode("remove")))
              .build();
      assertThat(query.toString()).isEqualTo("key=keep");
    }

    @Test
    void remove_nonexistent_key_is_no_op() {
      Query query = Query.builder().append("a", "1").remove("nonexistent").build();
      assertThat(query.toString()).isEqualTo("a=1");
    }

    @Test
    void remove_with_string_key_and_specific_value() {
      Query query =
          Query.builder()
              .append("key", "keep")
              .append("key", "remove")
              .append("key", "also-keep")
              .remove("key", "remove")
              .build();
      assertThat(query.toString()).isEqualTo("key=keep&key=also-keep");
    }

    @Test
    void remove_with_string_key_and_multiple_specific_values() {
      Query query =
          Query.builder()
              .append("key", "a")
              .append("key", "b")
              .append("key", "c")
              .append("key", "d")
              .remove("key", "b", "d")
              .build();
      assertThat(query.toString()).isEqualTo("key=a&key=c");
    }

    @Test
    void remove_with_typed_key_and_varargs_values() {
      Query query =
          Query.builder()
              .append("key", "keep")
              .append("key", "remove")
              .remove(QueryParamKey.encode("key"), QueryParamValue.encode("remove"))
              .build();
      assertThat(query.toString()).isEqualTo("key=keep");
    }

    @Test
    void encodes_special_characters_in_keys() {
      Query query = Query.builder().append("key=with=equals", "value").build();
      assertThat(query.toString()).isEqualTo("key%3Dwith%3Dequals=value");
    }

    @Test
    void encodes_special_characters_in_values() {
      Query query = Query.builder().append("key", "value&with&ampersands").build();
      assertThat(query.toString()).isEqualTo("key=value%26with%26ampersands");
    }

    @Test
    void chained_operations() {
      Query query =
          Query.builder()
              .append("a", "1")
              .append("b", "2")
              .append("c", "3")
              .put("b", "replaced")
              .remove("c")
              .append("d", "4")
              .build();
      assertThat(query.toString()).isEqualTo("a=1&b=replaced&d=4");
    }

    @Test
    void builder_is_reusable_for_multiple_builds() {
      Query.Builder builder = Query.builder().append("shared", "value");

      Query query1 = builder.append("extra1", "a").build();
      // Note: builder state is modified, so this tests current behavior
      Query query2 = builder.append("extra2", "b").build();

      assertThat(query1.contains("shared")).isTrue();
      assertThat(query2.contains("shared")).isTrue();
    }
  }

  @Nested
  class Empty {

    @Test
    void empty_constant_has_empty_string_representation() {
      assertThat(Query.EMPTY.toString()).isEmpty();
    }

    @Test
    void empty_constant_has_no_keys() {
      assertThat(Query.EMPTY.getKeys()).isEqualTo(Set.of());
    }

    @Test
    void empty_constant_equals_parsed_empty_string() {
      assertThat(Query.EMPTY).isEqualTo(Query.parse(""));
    }

    @Test
    void empty_constant_equals_builder_built_empty() {
      assertThat(Query.EMPTY).isEqualTo(Query.builder().build());
    }
  }

  @Nested
  class With {

    @Test
    void with_adds_param_to_existing_query() {
      Query original = Query.parse("a=1");
      Query updated =
          original.with(QueryParamKey.encode("b"), List.of(QueryParamValue.encode("2")));
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void with_preserves_original_query() {
      Query original = Query.parse("a=1");
      original.with(QueryParamKey.encode("b"), List.of(QueryParamValue.encode("2")));
      assertThat(original.toString()).isEqualTo("a=1");
    }

    @Test
    void with_adds_multiple_values_for_same_key() {
      Query original = Query.parse("a=1");
      Query updated =
          original
              .with(QueryParamKey.encode("b"), List.of(QueryParamValue.encode("2")))
              .with(QueryParamKey.encode("b"), List.of(QueryParamValue.encode("3")));
      assertThat(updated.toString()).isEqualTo("a=1&b=2&b=3");
    }

    @Test
    void with_adds_null_value() {
      Query original = Query.parse("a=1");
      List<@Nullable QueryParamValue> nullValue = Arrays.asList((QueryParamValue) null);
      Query updated = original.with(QueryParamKey.encode("b"), nullValue);
      assertThat(updated.toString()).isEqualTo("a=1&b");
    }

    @Test
    void with_list_of_values() {
      Query original = Query.parse("a=1");
      Query updated =
          original.with(
              QueryParamKey.encode("b"),
              List.of(QueryParamValue.encode("2"), QueryParamValue.encode("3")));
      assertThat(updated.toString()).isEqualTo("a=1&b=2&b=3");
    }

    @Test
    void with_encodes_special_characters() {
      Query original = Query.parse("a=1");
      Query updated =
          original.with(
              QueryParamKey.encode("key=special"),
              List.of(QueryParamValue.encode("value&special")));
      assertThat(updated.toString()).isEqualTo("a=1&key%3Dspecial=value%26special");
    }

    @Test
    void with_string_key_and_value() {
      Query original = Query.parse("a=1");
      Query updated = original.with("b", "2");
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void with_string_key_and_multiple_values() {
      Query original = Query.parse("a=1");
      Query updated = original.with("b", "2", "3", "4");
      assertThat(updated.toString()).isEqualTo("a=1&b=2&b=3&b=4");
    }

    @Test
    void with_string_key_and_null_value() {
      Query original = Query.parse("a=1");
      Query updated = original.with("b", null);
      assertThat(updated.toString()).isEqualTo("a=1&b");
    }

    @Test
    void with_typed_key_and_varargs_values() {
      Query original = Query.parse("a=1");
      Query updated =
          original.with(
              QueryParamKey.encode("b"), QueryParamValue.encode("2"), QueryParamValue.encode("3"));
      assertThat(updated.toString()).isEqualTo("a=1&b=2&b=3");
    }
  }

  @Nested
  class Replace {

    @Test
    void replace_replaces_existing_param() {
      Query original = Query.parse("a=1&b=2");
      Query updated =
          original.replace(QueryParamKey.encode("a"), List.of(QueryParamValue.encode("new")));
      assertThat(updated.toString()).isEqualTo("b=2&a=new");
    }

    @Test
    void replace_preserves_original_query() {
      Query original = Query.parse("a=1");
      original.replace(QueryParamKey.encode("a"), List.of(QueryParamValue.encode("new")));
      assertThat(original.toString()).isEqualTo("a=1");
    }

    @Test
    void replace_adds_if_key_not_present() {
      Query original = Query.parse("a=1");
      Query updated =
          original.replace(QueryParamKey.encode("b"), List.of(QueryParamValue.encode("2")));
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void replace_replaces_all_values_for_key() {
      Query original = Query.parse("a=1&a=2&a=3&b=x");
      Query updated =
          original.replace(QueryParamKey.encode("a"), List.of(QueryParamValue.encode("new")));
      assertThat(updated.toString()).isEqualTo("b=x&a=new");
      assertThat(updated.get("a")).containsExactly(QueryParamValue.parse("new"));
    }

    @Test
    void replace_with_multiple_values() {
      Query original = Query.parse("a=1&b=2");
      Query updated =
          original.replace(
              QueryParamKey.encode("a"),
              List.of(
                  QueryParamValue.encode("x"),
                  QueryParamValue.encode("y"),
                  QueryParamValue.encode("z")));
      assertThat(updated.get("a"))
          .containsExactly(
              QueryParamValue.parse("x"), QueryParamValue.parse("y"), QueryParamValue.parse("z"));
    }

    @Test
    void replace_with_list_of_values() {
      Query original = Query.parse("a=1");
      Query updated =
          original.replace(
              QueryParamKey.encode("a"),
              List.of(QueryParamValue.encode("x"), QueryParamValue.encode("y")));
      assertThat(updated.get("a"))
          .containsExactly(QueryParamValue.parse("x"), QueryParamValue.parse("y"));
    }

    @Test
    void replace_with_string_key_and_value() {
      Query original = Query.parse("a=1&b=2");
      Query updated = original.replace("a", "new");
      assertThat(updated.toString()).isEqualTo("b=2&a=new");
    }

    @Test
    void replace_with_string_key_and_multiple_values() {
      Query original = Query.parse("a=1&b=2");
      Query updated = original.replace("a", "x", "y", "z");
      assertThat(updated.get("a"))
          .containsExactly(
              QueryParamValue.parse("x"), QueryParamValue.parse("y"), QueryParamValue.parse("z"));
    }

    @Test
    void replace_with_typed_key_and_varargs_values() {
      Query original = Query.parse("a=1");
      Query updated =
          original.replace(
              QueryParamKey.encode("a"), QueryParamValue.encode("x"), QueryParamValue.encode("y"));
      assertThat(updated.get("a"))
          .containsExactly(QueryParamValue.parse("x"), QueryParamValue.parse("y"));
    }
  }

  @Nested
  class Without {

    @Test
    void without_removes_param_by_key() {
      Query original = Query.parse("a=1&b=2&c=3");
      Query updated = original.without("b");
      assertThat(updated.toString()).isEqualTo("a=1&c=3");
    }

    @Test
    void without_preserves_original_query() {
      Query original = Query.parse("a=1&b=2");
      original.without("a");
      assertThat(original.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void without_removes_all_values_for_key() {
      Query original = Query.parse("a=1&a=2&a=3&b=x");
      Query updated = original.without("a");
      assertThat(updated.toString()).isEqualTo("b=x");
      assertThat(updated.contains("a")).isFalse();
    }

    @Test
    void without_nonexistent_key_returns_equivalent_query() {
      Query original = Query.parse("a=1&b=2");
      Query updated = original.without("nonexistent");
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void without_typed_key() {
      Query original = Query.parse("a=1&b=2");
      Query updated = original.without(QueryParamKey.encode("a"));
      assertThat(updated.toString()).isEqualTo("b=2");
    }

    @Test
    void without_specific_value() {
      Query original = Query.parse("a=1&a=2&a=3");
      Query updated =
          original.without(QueryParamKey.encode("a"), List.of(QueryParamValue.encode("2")));
      assertThat(updated.toString()).isEqualTo("a=1&a=3");
    }

    @Test
    void without_multiple_specific_values() {
      Query original = Query.parse("a=1&a=2&a=3&a=4");
      Query updated =
          original.without(
              QueryParamKey.encode("a"),
              List.of(QueryParamValue.encode("2"), QueryParamValue.encode("4")));
      assertThat(updated.toString()).isEqualTo("a=1&a=3");
    }

    @Test
    void without_string_key_and_specific_value() {
      Query original = Query.parse("a=1&a=2&a=3");
      Query updated = original.without("a", "2");
      assertThat(updated.toString()).isEqualTo("a=1&a=3");
    }

    @Test
    void without_string_key_and_multiple_specific_values() {
      Query original = Query.parse("a=1&a=2&a=3&a=4");
      Query updated = original.without("a", "2", "4");
      assertThat(updated.toString()).isEqualTo("a=1&a=3");
    }

    @Test
    void without_typed_key_and_varargs_values() {
      Query original = Query.parse("a=1&a=2&a=3");
      Query updated =
          original.without(
              QueryParamKey.encode("a"), QueryParamValue.encode("2"), QueryParamValue.encode("3"));
      assertThat(updated.toString()).isEqualTo("a=1");
    }
  }

  @Nested
  class Thaw {

    @Test
    void thaw_returns_builder_with_same_content() {
      Query original = Query.parse("a=1&b=2");
      Query.Builder builder = original.thaw();
      Query rebuilt = builder.build();
      assertThat(rebuilt).isEqualTo(original);
    }

    @Test
    void thaw_builder_can_be_modified() {
      Query original = Query.parse("a=1");
      Query.Builder builder = original.thaw();
      builder.append("b", "2");
      Query updated = builder.build();
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void thaw_does_not_modify_original() {
      Query original = Query.parse("a=1");
      Query.Builder builder = original.thaw();
      builder.append("b", "2");
      builder.build();
      assertThat(original.toString()).isEqualTo("a=1");
    }

    @Test
    void thaw_empty_query() {
      Query original = Query.parse("");
      Query.Builder builder = original.thaw();
      builder.append("a", "1");
      Query updated = builder.build();
      assertThat(updated.toString()).isEqualTo("a=1");
    }
  }

  @Nested
  class Transform {

    @Test
    void transform_applies_modification() {
      Query original = Query.parse("a=1");
      Query updated = original.transform(b -> b.append("b", "2"));
      assertThat(updated.toString()).isEqualTo("a=1&b=2");
    }

    @Test
    void transform_preserves_original() {
      Query original = Query.parse("a=1");
      original.transform(b -> b.append("b", "2"));
      assertThat(original.toString()).isEqualTo("a=1");
    }

    @Test
    void transform_with_multiple_operations() {
      Query original = Query.parse("a=1&b=2&c=3");
      Query updated =
          original.transform(
              b -> {
                b.remove("b");
                b.put("a", "replaced");
                b.append("d", "4");
              });
      assertThat(updated.toString()).isEqualTo("c=3&a=replaced&d=4");
    }

    @Test
    void transform_can_clear_and_rebuild() {
      Query original = Query.parse("a=1&b=2");
      Query updated =
          original.transform(
              b -> {
                b.remove("a");
                b.remove("b");
                b.append("x", "new");
              });
      assertThat(updated.toString()).isEqualTo("x=new");
    }

    @Test
    void transform_chained() {
      Query original = Query.parse("a=1");
      Query updated =
          original
              .transform(b -> b.append("b", "2"))
              .transform(b -> b.append("c", "3"))
              .transform(b -> b.remove("a"));
      assertThat(updated.toString()).isEqualTo("b=2&c=3");
    }
  }
}
