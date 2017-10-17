package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import com.google.common.base.Optional;

class DiffLine<V> {

    private final String requestAttribute;
    private final NamedValueMatcher<V> pattern;
    private final V value;
    private final String printedPatternValue;

    public DiffLine(String requestAttribute, NamedValueMatcher<V> pattern, V value, String printedPatternValue) {
        this.requestAttribute = requestAttribute;
        this.pattern = pattern;
        this.value = value;
        this.printedPatternValue = printedPatternValue;
    }

    public Object getExpected() {
        return shouldBeIncluded() ?
            printedPatternValue :
            value;
    }

    public Object getActual() {
        return value;
    }

    public boolean shouldBeIncluded() {
        return !isExactMatch();
    }

    public boolean isExactMatch() {
        return pattern.match(value).isExactMatch();
    }

    public String getMessage() {
        return isExactMatch() ?
            null :
            requestAttribute + " does not match";
    }
}
