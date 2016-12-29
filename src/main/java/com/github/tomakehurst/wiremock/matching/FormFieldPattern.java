package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public class FormFieldPattern {
    private final StringValuePattern namePattern;
    private final StringValuePattern valuePattern;

    public FormFieldPattern(@JsonProperty("name") StringValuePattern namePattern, @JsonProperty("value") StringValuePattern valuePattern) {
        this.namePattern = namePattern;
        this.valuePattern = valuePattern;
    }

    @JsonProperty("name")
    public StringValuePattern getNamePattern() {
        return namePattern;
    }

    @JsonProperty("value")
    public StringValuePattern getValuePattern() {
        return valuePattern;
    }
}
