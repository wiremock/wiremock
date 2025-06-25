/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import static com.github.tomakehurst.wiremock.admin.Conversions.filterPredicates;
import static com.github.tomakehurst.wiremock.admin.Conversions.filterQueryParam;
import static com.github.tomakehurst.wiremock.admin.Conversions.toPredicate;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.common.Timing;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class ConversionsTest {

  @Test
  void mapsValidFirstParameterValueAsDate() {
    // given
    var queryParameter = new QueryParameter("since", List.of("2023-10-07T00:00:00Z"));
    var expected =
        Date.from(LocalDate.of(2023, Month.OCTOBER, 7).atStartOfDay(ZoneId.of("UTC")).toInstant());

    // when
    var result = Conversions.toDate(queryParameter);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void throwsExceptionWhenFirstParameterValueIsInvalidDate() {
    // given
    var queryParameter = new QueryParameter("since", List.of("invalid"));

    // when + then
    assertThrows(InvalidInputException.class, () -> Conversions.toDate(queryParameter));
  }

  final String url = "endpoint?key1=val1&key1=val1b&key2=val2";
  final ServeEvent serveEvent = getTestServeEvent(url);

  @Test
  void emptyExcludesIncludesTrue() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of());
    QueryParameter include = new QueryParameter("include", List.of());
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertTrue(predicate.test(serveEvent));
  }

  @Test
  void includesMatchTrue() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of());
    QueryParameter include = new QueryParameter("include", List.of("key1"));

    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);

    // then
    assertTrue(predicate.test(serveEvent));
  }

  @Test
  void includesAndMatchTrue() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of());
    QueryParameter include = new QueryParameter("include", List.of("key1=val1", "key1=val1b"));
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertTrue(predicate.test(serveEvent));
  }

  @Test
  void includesOrMatchTrue() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of());
    QueryParameter include = new QueryParameter("include", List.of("key1,key3"));
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertTrue(predicate.test(serveEvent));
  }

  @Test
  void includesUnmatchedFalse() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of());
    QueryParameter include = new QueryParameter("include", List.of("k3"));
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertFalse(predicate.test(serveEvent));
  }

  @Test
  void excludesMatchFalse() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of("key1"));
    QueryParameter include = new QueryParameter("include", List.of());
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertFalse(predicate.test(serveEvent));
  }

  @Test
  void excludesUnmatchedTrue() {
    // given
    QueryParameter exclude = new QueryParameter("exclude", List.of("k3"));
    QueryParameter include = new QueryParameter("include", List.of());
    // when
    Predicate<ServeEvent> predicate = toPredicate(exclude, include);
    // then
    assertTrue(predicate.test(serveEvent));
  }

  @Test
  void filterQueryParamTest() {
    Predicate<QueryParameter> predicate = filterQueryParam("foobar", true);
    QueryParameter fooQP = new QueryParameter("foo", List.of("foo"));
    QueryParameter barQP = new QueryParameter("bar", List.of("bar"));
    QueryParameter bazQP = new QueryParameter("baz", List.of("baz"));
    assertTrue(predicate.test(fooQP));
    assertTrue(predicate.test(barQP));
    assertFalse(predicate.test(bazQP));
  }

  @Test
  void filterPredicatesTest() {
    Predicate<List<String>> foobar = filterPredicates("foobar");
    assertTrue(foobar.test(List.of("foo")));
    assertTrue(foobar.test(List.of("bar")));
    assertTrue(foobar.test(List.of("foo,bar")));
    assertTrue(foobar.test(List.of("foo,baz")));
    assertTrue(foobar.test(List.of("baz,bar")));
    assertFalse(foobar.test(List.of("baz")));
  }

  @SuppressWarnings("SameParameterValue")
  static ServeEvent getTestServeEvent(String url) {
    UUID id = UUID.randomUUID();
    LoggedRequest request = getTestRequest(url);
    StubMapping stubMapping = null;
    ResponseDefinition responseDefinition = null;
    LoggedResponse response = null;
    boolean ignoredReadOnly = false;
    Timing timing = null;
    Queue<SubEvent> subEvents = null;

    //noinspection ConstantValue
    return new ServeEvent(
        id, request, stubMapping, responseDefinition, response, ignoredReadOnly, timing, subEvents);
  }

  public static LoggedRequest getTestRequest(String url) {
    MockRequest request = mockRequest();
    request.url(url);
    request.absoluteUrl("http://my.domain/" + url);
    return LoggedRequest.createFrom(request);
  }
}
