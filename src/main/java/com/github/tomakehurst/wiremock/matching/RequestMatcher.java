package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.Request;

public interface RequestMatcher {
    boolean isMatchedBy(Request request);
}
