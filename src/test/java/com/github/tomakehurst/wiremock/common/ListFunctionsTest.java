package com.github.tomakehurst.wiremock.common;

import org.junit.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.common.ListFunctions.splitByType;
import static com.github.tomakehurst.wiremock.common.Pair.pair;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class ListFunctionsTest {

    @Test
    public void emptyArrayReturnsTwoEmptyLists() {
        Number[] input = new Number[0];

        assertEquals(
            pair(Collections.<Number>emptyList(), Collections.<Integer>emptyList()),
            splitByType(input, Integer.class)
        );
    }

    @Test
    public void singletonArrayNonMatchingReturnsSingletonAndEmptyList() {
        Number[] input = new Number[] { 1L };

        assertEquals(
                pair(singletonList(1L), Collections.<Integer>emptyList()),
                splitByType(input, Integer.class)
        );
    }

    @Test
    public void singletonArrayMatchingReturnsEmptyAndSingletonList() {
        Number[] input = new Number[] { 1 };

        assertEquals(
                pair(Collections.<Number>emptyList(), singletonList(1)),
                splitByType(input, Integer.class)
        );
    }

    @Test
    public void splitsTheArrayAsExpected() {
        Number[] input = new Number[] { 1, 1L, 2, 2L, 3, 3L };

        assertEquals(
                pair(asList(1L, 2L, 3L), asList(1, 2, 3)),
                splitByType(input, Integer.class)
        );
    }
}
