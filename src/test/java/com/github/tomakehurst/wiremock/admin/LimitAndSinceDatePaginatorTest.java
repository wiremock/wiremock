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
package com.github.tomakehurst.wiremock.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LimitAndSinceDatePaginatorTest {

  private ServeEvent newEvent(String url) {
    // LoggedDate is set when creating LoggedRequest from Request
    return ServeEvent.of(MockRequest.mockRequest().url(url).method(RequestMethod.GET));
  }

  @Test
  void returnsWholeListWhenBothParametersAreNull() {
    List<ServeEvent> source = List.of(newEvent("/one"), newEvent("/two"), newEvent("/three"));

    LimitAndSinceDatePaginator paginator = new LimitAndSinceDatePaginator(source, null, null);

    List<ServeEvent> result = paginator.select();

    assertThat(result, is(source));
  }

  @Test
  void filtersOnlyEventsAfterSinceDate_excludingEqual() throws Exception {
    ServeEvent first = newEvent("/first");
    // Ensure distinct loggedDate ordering
    TimeUnit.MILLISECONDS.sleep(10);
    ServeEvent second = newEvent("/second");

    List<ServeEvent> source = List.of(first, second);

    LimitAndSinceDatePaginator paginator =
        new LimitAndSinceDatePaginator(source, null, first.getRequest().getLoggedDate());

    List<ServeEvent> result = paginator.select();

    // Only the event strictly after 'since' should be included
    assertThat(result, is(List.of(second)));
  }

  @Test
  void respectsLimitWhenProvided() {
    List<ServeEvent> source = List.of(newEvent("/a"), newEvent("/b"), newEvent("/c"));

    LimitAndSinceDatePaginator paginator = new LimitAndSinceDatePaginator(source, 2, null);

    List<ServeEvent> result = paginator.select();

    assertThat(result.size(), is(2));
    assertThat(result.get(0).getRequest().getUrl(), is("/a"));
    assertThat(result.get(1).getRequest().getUrl(), is("/b"));
  }

  @Test
  void totalIsSizeOfOriginalSource() {
    List<ServeEvent> source = List.of(newEvent("/x"), newEvent("/y"));

    LimitAndSinceDatePaginator paginator = new LimitAndSinceDatePaginator(source, 1, null);

    assertThat(paginator.getTotal(), is(2));
  }

  @Test
  void buildsFromRequestAndAppliesFilters() throws Exception {
    ServeEvent first = newEvent("/one");
    TimeUnit.MILLISECONDS.sleep(10);
    ServeEvent second = newEvent("/two");

    List<ServeEvent> source = List.of(first, second);

    String sinceIso =
        ZonedDateTime.ofInstant(first.getRequest().getLoggedDate().toInstant(), ZoneId.of("UTC"))
            .toString();

    // Build a request with limit=1 and since=<first's logged date>
    var request =
        MockRequest.mockRequest()
            .url("/events?limit=1&since=" + sinceIso)
            .method(RequestMethod.GET);

    LimitAndSinceDatePaginator paginator = LimitAndSinceDatePaginator.fromRequest(source, request);

    List<ServeEvent> result = paginator.select();

    // Only one event should be returned due to limit, and it should be '/two' as it's after 'since'
    assertThat(result.size(), is(1));
    assertThat(result.get(0).getRequest().getUrl(), is("/two"));
  }
}
