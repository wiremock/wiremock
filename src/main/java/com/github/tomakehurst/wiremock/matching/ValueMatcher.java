package com.github.tomakehurst.wiremock.matching;

public interface ValueMatcher<T> {

    MatchResult match(T value);
}
