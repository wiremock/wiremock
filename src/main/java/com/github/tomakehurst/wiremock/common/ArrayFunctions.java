package com.github.tomakehurst.wiremock.common;

import static java.util.Arrays.copyOf;

public final class ArrayFunctions {

    public static <T> T[] concat(T[] first, T[] second) {
        if (first.length + second.length == 0) {
            return first;
        }
        T[] both = copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    private ArrayFunctions() {
        throw new UnsupportedOperationException("not instantiable");
    }
}
