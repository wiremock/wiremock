/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.url.QueryParams;
import org.junit.jupiter.api.Test;

public class QueryParamsTest {

  @Test
  public void returnsEmptyStringWhenNoParametersPresent() {
    assertThat(QueryParams.EMPTY.toString(), is(""));
  }

  @Test
  public void correctlyRendersASingleQueryParamWithSingleValueAsString() {
    assertThat(QueryParams.single("param", "123").toString(), is("?param=123"));
  }

  @Test
  public void correctlyRendersASingleQueryParamWithMultipleValuesAsString() {
    assertThat(
        QueryParams.single("param", "123", "blah", "456").toString(),
        is("?param=123&param=blah&param=456"));
  }

  @Test
  public void correctlyRendersMultipleQueryParamsWithMixedSingleAndMultipleValuesAsString() {
    QueryParams queryParams = new QueryParams();
    queryParams.put("one", singletonList("1"));
    queryParams.put("two", asList("2", "three"));
    assertThat(queryParams.toString(), is("?one=1&two=2&two=three"));
  }
}
