package com.github.tomakehurst.wiremock.extension.requestfilter;

public abstract class StubRequestFilter implements RequestFilter {

    @Override
    public boolean applyToAdmin() {
        return false;
    }

    @Override
    public boolean applyToStubs() {
        return true;
    }
}
