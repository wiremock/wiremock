package com.github.tomakehurst.wiremock.matching.optional;

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public abstract class OptionalPattern extends StringValuePattern {

    protected StringValuePattern pattern;

    public OptionalPattern(final StringValuePattern pattern) {
        super(pattern.getValue());
        this.pattern = pattern;
    }

    @Override
    public MatchResult match(final String value) {
        final MatchResult patternMatchResult = pattern.match(value);
        final MatchResult absentMatchResult = ABSENT.match(value);

        return MatchResult.of(patternMatchResult.isExactMatch() || absentMatchResult.isExactMatch());
    }
}
