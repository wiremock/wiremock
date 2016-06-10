package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ValueMatcher<T> {

    MatchResult match(T value);

    @JsonIgnore
    String getName();

    @JsonIgnore
    String getExpected();
}
