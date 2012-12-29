package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public interface StubServer {

    ResponseDefinition serveStubFor(Request request);
}
