package com.github.tomakehurst.wiremock;

public class NotImplementedSocketControl implements SocketControl {
    @Override
    public void setDelay(int requestCount, long milliseconds) {
        throw new UnsupportedOperationException("Socket control isn't available in this configuration");
    }

    @Override
    public void delayIfRequired() {
        // Do nothing
    }
}
