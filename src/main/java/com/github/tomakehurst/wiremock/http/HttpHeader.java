package com.github.tomakehurst.wiremock.http;

import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Sets.newHashSet;

public class HttpHeader {

    private final String key;
    private final Set<String> values;

    public HttpHeader(String key, String... values) {
        this.key = key;
        this.values = newHashSet(values);
    }

    public static HttpHeader httpHeader(String key, String... values) {
        return new HttpHeader(key, values);
    }

    public static HttpHeader absent(String key) {
        return new HttpHeader(key);
    }

    public boolean isPresent() {
        return values.size() > 0;
    }

    public String key() {
        return key;
    }

    public String firstValue() {
        checkState(isPresent());
        return getFirst(values, null);
    }

    public Set<String> values() {
        checkState(isPresent());
        return values;
    }

    public boolean containsValue(String expectedValue) {
        return values.contains(expectedValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String value: values) {
            sb.append(key).append(": ").append(value).append("\n");
        }

        return sb.toString();
    }

}
