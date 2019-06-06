package com.github.tomakehurst.wiremock.extension.requestfilter;

public interface FieldTransformer<T> {

    T transform(T source);
}
