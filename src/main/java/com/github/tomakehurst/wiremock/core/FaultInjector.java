package com.github.tomakehurst.wiremock.core;

public interface FaultInjector {

    void emptyResponseAndCloseConnection();
    void malformedResponseChunk();
    void randomDataAndCloseConnection();
}
