package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonValue
    public String getValue() {
        return value;
    }
}
