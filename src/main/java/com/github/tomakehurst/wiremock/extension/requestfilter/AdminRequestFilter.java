package com.github.tomakehurst.wiremock.extension.requestfilter;

public abstract class AdminRequestFilter implements RequestFilter {

    @Override
    public boolean applyToAdmin() {
        return true;
    }

    @Override
    public boolean applyToStubs() {
        return false;
    }
}
