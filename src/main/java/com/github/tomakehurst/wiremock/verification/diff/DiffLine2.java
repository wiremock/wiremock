package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.http.content.Content;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;

public class DiffLine2<V> {

    private final String key;
    private final ContentPattern pattern;
    private final Content value;

    public DiffLine2(NamedValueMatcher<V> pattern, Content value) {
        this(null, pattern, value);
    }

    public DiffLine2(String key, ContentPattern pattern, Content value) {
        this.key = key;
        this.pattern = pattern;
        this.value = value;
    }

    public boolean isExactMatch() {
        return pattern.match(value).isExactMatch();
    }

    public String printExpected() {
        return pattern.getExpected();
    }

    public String printActual() {
        return value.getAsString();
    }
}
