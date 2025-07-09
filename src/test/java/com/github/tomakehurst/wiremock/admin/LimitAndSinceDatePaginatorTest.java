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
package com.github.tomakehurst.wiremock.admin;

import static com.github.tomakehurst.wiremock.admin.ConversionsTest.getTestServeEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class LimitAndSinceDatePaginatorTest {

  @Test
  void filterRequestsTest() {
    QueryParameter exclude = new QueryParameter("exclude", List.of("health_check", "favicon.ico"));
    QueryParameter include = new QueryParameter("include", List.of("foo", "bar"));
    Predicate<ServeEvent> serveEventPredicate = Conversions.toPredicate(exclude, include);

    List<ServeEvent> serveEventList = new ArrayList<>();
    serveEventList.add(getTestServeEvent("/health_check"));
    serveEventList.add(getTestServeEvent("/health_check"));
    serveEventList.add(getTestServeEvent("/favicon.ico"));
    serveEventList.add(getTestServeEvent("/foo"));
    serveEventList.add(getTestServeEvent("/foo"));
    serveEventList.add(getTestServeEvent("/bar"));
    serveEventList.add(getTestServeEvent("/baz"));

    LimitAndSinceDatePaginator limitAndSinceDatePaginator =
        new LimitAndSinceDatePaginator(serveEventList, 10, null, serveEventPredicate);

    assertEquals(3, limitAndSinceDatePaginator.getTotal());
    List<String> expectedUrls = List.of("/foo", "/foo", "/bar");
    List<String> actualUrls =
        limitAndSinceDatePaginator.select().stream()
            .map(ServeEvent::getRequest)
            .map(Request::getUrl)
            .toList();
    assertEquals(expectedUrls, actualUrls);
  }
}
