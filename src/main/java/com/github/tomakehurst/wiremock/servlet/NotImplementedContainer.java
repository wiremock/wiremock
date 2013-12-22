package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.core.Container;

public class NotImplementedContainer implements Container {
    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Stopping the server is not supported");
    }
}
