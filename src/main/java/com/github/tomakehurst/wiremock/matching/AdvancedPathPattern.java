package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class AdvancedPathPattern {

    public final String expression;

    @JsonUnwrapped
    public final StringValuePattern submatcher;

    public AdvancedPathPattern(String expression, StringValuePattern submatcher) {
        this.expression = expression;
        this.submatcher = submatcher;
    }
}
