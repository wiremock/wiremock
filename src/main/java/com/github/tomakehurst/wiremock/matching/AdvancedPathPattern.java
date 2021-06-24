package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class AdvancedPathPattern {

    public final String expression;

    @JsonUnwrapped
    public final ContentPattern<?> submatcher;

    public AdvancedPathPattern(String expression, ContentPattern<?> submatcher) {
        this.expression = expression;
        this.submatcher = submatcher;
    }
}
