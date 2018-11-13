package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;

public class CustomMatcherWrapper implements NamedValueMatcher<Request> {

    private final ValueMatcher<Request> matcher;

    public CustomMatcherWrapper(ValueMatcher<Request> matcher) {
        this.matcher = matcher;
    }

    @Override
    public String getName() {
        return "custom matcher";
    }

    @Override
    public String getExpected() {
        return "[custom matcher]";
    }

    @Override
    public MatchResult match(Request value) {
        return matcher.match(value);
    }
}
