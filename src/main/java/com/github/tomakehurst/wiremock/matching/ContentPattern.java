package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ContentPatternDeserialiser.class)
public abstract class ContentPattern<T> implements NamedValueMatcher<T> {

    protected final T expectedValue;

    public ContentPattern(T expectedValue) {
        this.expectedValue = expectedValue;
    }

    @JsonIgnore
    public T getValue() {
        return expectedValue;
    }
}
