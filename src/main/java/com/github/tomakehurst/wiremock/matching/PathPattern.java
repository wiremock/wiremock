package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class PathPattern extends StringValuePattern {

    protected final StringValuePattern valuePattern;

    protected PathPattern(String expectedValue, StringValuePattern valuePattern) {
        super(expectedValue);
        this.valuePattern = valuePattern;
    }

    public StringValuePattern getValuePattern() {
        return valuePattern;
    }

    @JsonIgnore
    public boolean isSimple() {
        return valuePattern == null;
    }

    @Override
    public MatchResult match(String value) {
        if (isSimple()) {
            return isSimpleJsonPathMatch(value);
        }

        return isAdvancedJsonPathMatch(value);
    }

    protected abstract MatchResult isSimpleJsonPathMatch(String value);
    protected abstract MatchResult isAdvancedJsonPathMatch(String value);
}
