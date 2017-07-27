package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;

public class BinaryEqualToPattern extends ContentPattern<byte[]> {

    public BinaryEqualToPattern(byte[] expected) {
        super(expected);
    }

    @JsonCreator
    public BinaryEqualToPattern(@JsonProperty("binaryEqualTo") String expected) {
        this(BaseEncoding.base64().decode(expected));
    }

    @Override
    public MatchResult match(byte[] actual) {
        return MatchResult.of(
            Arrays.equals(actual, expectedValue)
        );
    }

    @Override
    @JsonIgnore
    public String getName() {
        return "binaryEqualTo";
    }

    @Override
    @JsonIgnore
    public String getExpected() {
        return BaseEncoding.base64().encode(expectedValue);
    }

    public String getBinaryEqualTo() {
        return getExpected();
    }

    @Override
    public String toString() {
        return getName() + " " + getExpected();
    }
}
