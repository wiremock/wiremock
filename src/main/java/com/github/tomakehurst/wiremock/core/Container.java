package com.github.tomakehurst.wiremock.core;

/**
 * A container of a WireMockApp instance
 */
public interface Container {
    /**
     * Shuts down the container, stopping execution of WireMock, gracefully if possible.
     */
    void shutdown();
}
