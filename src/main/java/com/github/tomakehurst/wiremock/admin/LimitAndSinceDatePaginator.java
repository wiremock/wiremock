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

import static com.github.tomakehurst.wiremock.admin.Conversions.toDate;
import static com.github.tomakehurst.wiremock.admin.Conversions.toInt;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A paginator for {@link ServeEvent} lists that filters by a "since" date and limits the result
 * size.
 *
 * <p>This implementation selects events that occurred after the specified date and then applies a
 * limit to the result set.
 */
public class LimitAndSinceDatePaginator implements Paginator<ServeEvent> {

  private final List<ServeEvent> source;
  private final Integer limit;
  private final Date since;

  /**
   * Constructs a new LimitAndSinceDatePaginator.
   *
   * @param source The source list of serve events to paginate.
   * @param limit The maximum number of items to return (can be null for no limit). Must be
   *     non-negative if provided.
   * @param since The date to filter events from; only events after this date will be included (can
   *     be null).
   */
  public LimitAndSinceDatePaginator(List<ServeEvent> source, Integer limit, Date since) {
    checkParameter(limit == null || limit >= 0, "limit must be 0 or greater");
    this.source = source;
    this.limit = limit;
    this.since = since;
  }

  /**
   * Creates a new paginator by extracting 'limit' and 'since' from request query parameters.
   *
   * @param source The source list to paginate.
   * @param request The incoming HTTP request.
   * @return A new {@code LimitAndSinceDatePaginator} instance.
   */
  public static LimitAndSinceDatePaginator fromRequest(List<ServeEvent> source, Request request) {
    return new LimitAndSinceDatePaginator(
        source, toInt(request.queryParameter("limit")), toDate(request.queryParameter("since")));
  }

  @Override
  public List<ServeEvent> select() {
    return source.stream()
        .filter(input -> since == null || input.getRequest().getLoggedDate().after(since))
        .limit(getFirstNonNull(limit, source.size()))
        .collect(Collectors.toList());
  }

  @Override
  public int getTotal() {
    return source.size();
  }
}
