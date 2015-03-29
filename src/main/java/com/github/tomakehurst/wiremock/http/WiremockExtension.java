package com.github.tomakehurst.wiremock.http;

/**
 * Implementations must have a no-args constructor.
 */
public interface WiremockExtension {
    ResponseDefinition transform(Request request, ResponseDefinition responseDefinition);
    String getName();
    boolean isGlobal();
}
