package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.Request;

public class DoNothingMatcherObserver implements MatcherObserver {
    public DoNothingMatcherObserver() {
    }

    @Override
    public void onMatchingResult(boolean requestMatchesPattern, boolean urlMatches, boolean methodMatches, boolean requiredAbsentHeadersAreNotPresent, boolean headersMatch, boolean queryParametersMatch, boolean bodyMatches, Request request, RequestPattern requestPattern) {
        // do nothing
    }
}
