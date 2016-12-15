package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class ListOrSingle<T> extends ArrayList<T> {

    public ListOrSingle(Collection<? extends T> c) {
        super(c);
    }

    public ListOrSingle(T... items) {
        this(asList(items));
    }

    @Override
    public String toString() {
        return size() > 0 ? get(0).toString() : "";
    }

    public static <T> ListOrSingle<T> of(T... items) {
        return new ListOrSingle<>(items);
    }

    public static <T> ListOrSingle<T> of(List<T> items) {
        return new ListOrSingle<>(items);
    }
}
