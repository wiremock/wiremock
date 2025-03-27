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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.http.Request;
import java.util.List;

public class LimitAndOffsetPaginator<T> implements Paginator<T> {

  private final List<T> source;
  private final Integer limit;
  private final Integer offset;

  public LimitAndOffsetPaginator(List<T> source, Integer limit, Integer offset) {
    this.source = source;
    checkParameter(limit == null || limit >= 0, "limit must be 0 or greater");
    checkParameter(offset == null || offset >= 0, "offset must be 0 or greater");
    this.limit = limit;
    this.offset = offset;
  }

  public static <T> LimitAndOffsetPaginator<T> fromRequest(List<T> source, Request request) {
    return new LimitAndOffsetPaginator<>(
        source,
        Conversions.toInt(request.queryParameter("limit")),
        Conversions.toInt(request.queryParameter("offset")));
  }

  @Override
  public List<T> select() {
    int start = getFirstNonNull(offset, 0);
    int end = Math.min(source.size(), start + getFirstNonNull(limit, source.size()));

    return source.subList(start, end);
  }

  @Override
  public int getTotal() {
    return source.size();
  }

  public static <T> LimitAndOffsetPaginator<T> none(List<T> source) {
    return new LimitAndOffsetPaginator<>(source, null, null);
  }
}
