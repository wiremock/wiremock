package com.github.tomakehurst.wiremock.matching;

import org.skyscreamer.jsonassert.JSONCompareMode;

public abstract class StringValuePattern implements ValueMatcher<String> {

    protected final String testValue;

    public StringValuePattern(String testValue) {
        this.testValue = testValue;
    }

    public static StringValuePattern equalTo(String value) {
        return new EqualToPattern(value);
    }

    public static StringValuePattern equalToJson(String value) {
        return null;
    }

    public static StringValuePattern equalToXml(String value) {
        return null;
    }

    public static StringValuePattern equalToXPath(String value) {
        return null;
    }

    public static StringValuePattern equalToJson(String value, JSONCompareMode jsonCompareMode) {
        return null;
    }

    public static StringValuePattern containing(String value) {
        return null;
    }

    public static StringValuePattern matches(String regex) {
        return new RegexPattern(regex);
    }

    public static StringValuePattern absent() {
        return null;
    }

}
