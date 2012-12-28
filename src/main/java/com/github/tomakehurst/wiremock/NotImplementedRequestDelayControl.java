package com.github.tomakehurst.wiremock;

public class NotImplementedRequestDelayControl implements RequestDelayControl {

    @Override
    public void setDelay(int milliseconds) {
        throw new UnsupportedOperationException("Socket control isn't available in this configuration");
    }

    @Override
    public void clearDelay() {
        throw new UnsupportedOperationException("Socket control isn't available in this configuration");
    }

    @Override
    public void delayIfRequired() {
        // Do nothing
    }

}
