/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LimitAndOffsetPaginatorTest {

  @Test
  void returnsWholeListWhenBothParametersAreNull() {
    List<Integer> source = List.of(1, 2, 3, 4, 5);
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, null);

    List<Integer> result = paginator.select();

    assertThat(result, is(List.of(1, 2, 3, 4, 5)));
  }

  @Test
  void returnsEmptyListWhenSourceIsEmpty() {
    List<Integer> source = emptyList();
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, null);

    List<Integer> result = paginator.select();

    assertThat(result, is(Collections.emptyList()));
  }

  @Test
  void returnsTruncatedListFromStartWhenOnlyLimitIsSpecified() {
    List<Integer> source = List.of(1, 2, 3, 4, 5);
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 3, null);

    List<Integer> result = paginator.select();

    assertThat(result, is(List.of(1, 2, 3)));
  }

  @Test
  void returnsFromOffSetToTheEndWhenOnlyOffsetIsSpecified() {
    List<Integer> source = List.of(1, 2, 3, 4, 5);
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, 2);

    List<Integer> result = paginator.select();

    assertThat(result, is(List.of(3, 4, 5)));
  }

  @Test
  void returnsRangeWhenBothAreSpecified() {
    List<Integer> source = List.of(1, 2, 3, 4, 5);
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 3, 1);

    List<Integer> result = paginator.select();

    assertThat(result, is(List.of(2, 3, 4)));
  }

  @Test
  void returnsToEndOfListWhenTopBoundIsGreaterThanListSize() {
    List<Integer> source = List.of(1, 2, 3, 4, 5);
    LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 7, 3);

    List<Integer> result = paginator.select();

    assertThat(result, is(List.of(4, 5)));
  }

  @Test
  void rejectsNegativeLimit() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new LimitAndOffsetPaginator<>(Collections.emptyList(), -1, 3));
  }

  @Test
  void rejectsNegativeOffset() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new LimitAndOffsetPaginator<>(Collections.emptyList(), 0, -10));
  }
}
