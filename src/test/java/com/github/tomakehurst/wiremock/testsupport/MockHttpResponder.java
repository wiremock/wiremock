package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.http.HttpResponder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

public class MockHttpResponder implements HttpResponder {

    public Response response;

    @Override
    public void respond(Request request, Response response) {
        this.response = response;
    }
}
