package com.github.tomakehurst.wiremock.http;

public interface HttpResponder {
    void respond(Request request, Response response);
}
