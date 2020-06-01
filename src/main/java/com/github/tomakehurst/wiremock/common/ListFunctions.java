package com.github.tomakehurst.wiremock.common;

import java.util.ArrayList;
import java.util.List;

public final class ListFunctions {

    public static <A, B extends A> Pair<List<A>, List<B>> splitByType(A[] items, Class<B> subType) {
        List<A> as = new ArrayList<>();
        List<B> bs = new ArrayList<>();
        for (A a : items) {
            if (subType.isAssignableFrom(a.getClass())) {
                bs.add((B) a);
            } else {
                as.add(a);
            }
        }
        return new Pair<>(as, bs);
    }

    private ListFunctions() {
        throw new UnsupportedOperationException("Not instantiable");
    }
}
