package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.skyscreamer.jsonassert.JSONCompareMode;

@JsonSerialize(using = StringValuePatternJsonSerializer.class)
@JsonDeserialize(using = StringValuePatternJsonDeserializer.class)
public abstract class StringValuePattern implements ValueMatcher<String> {

    public static final StringValuePattern ABSENT = new StringValuePattern(null) {
        @Override
        public MatchResult match(String value) {
            return MatchResult.noMatch();
        }
    };

    protected final String testValue;

    public StringValuePattern(String testValue) {
        this.testValue = testValue;
    }

    public static StringValuePattern equalTo(String value) {
        return new EqualToPattern(value);
    }

    public static StringValuePattern equalToJson(String value, EqualToJsonPattern.Parameter... parameters) {
        return new EqualToJsonPattern(value, parameters);
    }

    public static StringValuePattern equalToXml(String value) {
        return null;
    }

    public static StringValuePattern equalToXPath(String value) {
        return null;
    }

    public static StringValuePattern containing(String value) {
        return null;
    }

    public static StringValuePattern matches(String regex) {
        return new RegexPattern(regex);
    }

    public static StringValuePattern absent() {
        return ABSENT;
    }

    public boolean isPresent() {
        return this != ABSENT;
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    @JsonValue
    public String getValue() {
        return testValue;
    }

    @Override
    public String toString() {
        return getName() + " " + getValue();
    }

    public String getName() {
        return "";
    }
}
