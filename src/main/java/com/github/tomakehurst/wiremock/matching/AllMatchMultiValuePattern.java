package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.MultiValue;

public class AllMatchMultiValuePattern extends MultiValuePattern {

    @Override
    public MatchResult match(MultiValue value) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getExpected() {
        return null;
    }

}
