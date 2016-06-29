package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class Cookie {

    private String value;

    @JsonCreator
    public static Cookie cookie(String value) {
        return new Cookie(value);
    }

    public static Cookie absent() {
        return new Cookie(null);
    }

    public Cookie(String value) {
        this.value = value;
    }

    @JsonIgnore
    public boolean isPresent() {
        return value != null;
    }

    @JsonIgnore
    public boolean isAbsent() {
        return value == null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return isAbsent() ? "(absent)" : value;
    }
}
