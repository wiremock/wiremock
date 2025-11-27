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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class QueryTests {

  static Stream<String> validQuery() {
    return Stream.of(
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
  }

  static Stream<String> invalidQueries() {
    return Stream.of("key=val#ue");
  }

  @ParameterizedTest
  @MethodSource("invalidQueries")
  void throws_exception_for_invalid_userinfo(String invalidQuery) {
    assertThatExceptionOfType(IllegalQuery.class)
        .isThrownBy(() -> Query.parse(invalidQuery))
        .withMessage("Illegal query: `" + invalidQuery + "`")
        .extracting(IllegalQuery::getIllegalValue)
        .isEqualTo(invalidQuery);
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return CharSequenceParserInvariantTests.generateInvariantTests(
        QueryParser.INSTANCE, validQuery().toList());
  }
}
