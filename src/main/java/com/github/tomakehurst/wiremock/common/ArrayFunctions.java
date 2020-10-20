package com.github.tomakehurst.wiremock.common;

import java.lang.reflect.Array;

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

    public static <T> T[] prepend(T t, T[] original) {
        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) Array.newInstance(original.getClass().getComponentType(), original.length + 1);
        newArray[0]= t;
        System.arraycopy(original, 0, newArray, 1, original.length);
        return newArray;
    }

    private ArrayFunctions() {
        throw new UnsupportedOperationException("not instantiable");
    }
}
