package com.github.tomakehurst.wiremock.junit;


public interface Delaying {

    void setGlobalFixedDelay(int milliseconds);
    void addRequestProcessingDelay(int milliseconds);
}
