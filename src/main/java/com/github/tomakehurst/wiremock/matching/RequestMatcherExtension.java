package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;

public abstract class RequestMatcherExtension implements Extension, RequestMatcher {

    @Override
    public boolean isMatchedBy(Request request) {
        return isMatchedBy(request, Parameters.empty());
    }

    public abstract boolean isMatchedBy(Request request, Parameters parameters);

    @Override
    public String name() {
        return "inline";
    }

    public static final RequestMatcherExtension ALWAYS = new RequestMatcherExtension() {
        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            return true;
        }
    };

    public static final RequestMatcherExtension NEVER = new RequestMatcherExtension() {
        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            return false;
        }
    };
}
