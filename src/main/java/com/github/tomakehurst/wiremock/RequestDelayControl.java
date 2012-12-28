package com.github.tomakehurst.wiremock;

public interface RequestDelayControl {

    void setDelay(int milliseconds);
    void clearDelay();
    void delayIfRequired() throws InterruptedException;
}
