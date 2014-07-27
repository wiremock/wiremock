package com.github.tomakehurst.wiremock.http;

import com.google.common.base.Function;

public class CaseInsensitiveKey {

    private final String key;
    public CaseInsensitiveKey(String key) {
        this.key = key;
    }

    public static CaseInsensitiveKey from(String key) {
        return new CaseInsensitiveKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaseInsensitiveKey that = (CaseInsensitiveKey) o;

        if (key != null ? !key.toLowerCase().equals(that.key.toLowerCase()) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.toLowerCase().hashCode() : 0;
    }

    @Override
    public String toString() {
        return key;
    }

    public String value() {
        return key;
    }

    public static final Function<String, CaseInsensitiveKey> TO_CASE_INSENSITIVE_KEYS = new Function<String, CaseInsensitiveKey>() {
        public CaseInsensitiveKey apply(String input) {
            return from(input);
        }
    };
}
