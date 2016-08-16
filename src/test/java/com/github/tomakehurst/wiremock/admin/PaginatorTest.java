package com.github.tomakehurst.wiremock.admin;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PaginatorTest {

    @Test
    public void returnsWholeListWhenBothParametersAreNull() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        Paginator<Integer> paginator = new Paginator<>(source, null, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void returnsEmptyListWhenSourceIsEmpty() {
        List<Integer> source = emptyList();
        Paginator<Integer> paginator = new Paginator<>(source, null, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(Collections.<Integer>emptyList()));
    }

    @Test
    public void returnsTruncatedListFromStartWhenOnlyLimitIsSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        Paginator<Integer> paginator = new Paginator<>(source, 3, null);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(1, 2, 3)));
    }

    @Test
    public void returnsFromOffSetToTheEndWhenOnlyOffsetIsSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        Paginator<Integer> paginator = new Paginator<>(source, null, 2);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(3, 4, 5)));
    }

    @Test
    public void returnsRangeWhenBothAreSpecified() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        Paginator<Integer> paginator = new Paginator<>(source, 3, 1);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(2, 3, 4)));
    }

    @Test
    public void returnsToEndOfListWhenTopBoundIsGreaterThanListSize() {
        List<Integer> source = asList(1, 2, 3, 4, 5);
        Paginator<Integer> paginator = new Paginator<>(source, 7, 3);

        List<Integer> result = paginator.select();

        assertThat(result, is(asList(4, 5)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeLimit() {
        new Paginator<>(Collections.<Void>emptyList(), -1, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeOffset() {
        new Paginator<>(Collections.<Void>emptyList(), 0, -10);
    }

}