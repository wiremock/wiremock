package com.github.tomakehurst.wiremock.common;

public interface Source<T> {
    T load();
    void save(T item);
    boolean exists();
}
