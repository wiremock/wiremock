package com.github.tomakehurst.wiremock.http;

public class CaseInsensitiveKey {

    private final String key;

    public CaseInsensitiveKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
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
        return key.toLowerCase() != null ? key.toLowerCase().hashCode() : 0;
    }
}
