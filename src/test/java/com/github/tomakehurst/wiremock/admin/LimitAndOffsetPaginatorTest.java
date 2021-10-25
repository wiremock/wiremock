/*
 * Copyright (C) 2011 Thomas Akehurst
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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LimitAndOffsetPaginatorTest {

    @Test
    public void returnsWholeListWhenBothParametersAreNull() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void returnsEmptyListWhenSourceIsEmpty() {
        List<Integer> source = emptyList();
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(Collections.<Integer>emptyList()));
    }

    @Test
    public void returnsTruncatedListFromStartWhenOnlyLimitIsSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 3, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(1, 2, 3)));
    }

    @Test
    public void returnsFromOffSetToTheEndWhenOnlyOffsetIsSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, null, 2);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(3, 4, 5)));
    }

    @Test
    public void returnsRangeWhenBothAreSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 3, 1);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(2, 3, 4)));
    }

    @Test
    public void returnsToEndOfListWhenTopBoundIsGreaterThanListSize() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        LimitAndOffsetPaginator<Integer> paginator = new LimitAndOffsetPaginator<>(source, 7, 3);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(4, 5)));
    }

    @Test
    public void rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LimitAndOffsetPaginator<>(Collections.<Void>emptyList(), -1, 3);
        });
    }

    @Test
    public void rejectsNegativeOffset() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LimitAndOffsetPaginator<>(Collections.<Void>emptyList(), 0, -10);
        });
    }

}