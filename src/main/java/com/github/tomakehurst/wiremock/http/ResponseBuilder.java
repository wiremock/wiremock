package com.github.tomakehurst.wiremock.http;

import com.google.common.base.Optional;

import static com.google.common.base.Charsets.UTF_8;

public class ResponseBuilder {

    private int status = 200;
    private byte[] body = "body text".getBytes();
    private HttpHeaders headers = HttpHeaders.noHeaders();
    private boolean configured = true;
    private Fault fault;
    private boolean fromProxy = false;

    public static ResponseBuilder aResponse() {
        return new ResponseBuilder();
    }

    public static ResponseBuilder like(Response response) {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.status = response.getStatus();
        responseBuilder.body = response.getBody();
        responseBuilder.headers = response.getHeaders();
        responseBuilder.configured = response.wasConfigured();
        responseBuilder.fault = response.getFault();
        responseBuilder.fromProxy = response.isFromProxy();
        return responseBuilder;
    }

    public ResponseBuilder but() {
        return this;
    }

    public ResponseBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public ResponseBuilder withBody(byte[] body) {
        this.body = body;
        return this;
    }

    public ResponseBuilder withBody(String body) {
        this.body = body.getBytes(UTF_8);
        return this;
    }

    public ResponseBuilder withHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public ResponseBuilder withConfigured(boolean configured) {
        this.configured = configured;
        return this;
    }

    public ResponseBuilder withFault(Fault fault) {
        this.fault = fault;
        return this;
    }

    public ResponseBuilder withFromProxy(boolean fromProxy) {
        this.fromProxy = fromProxy;
        return this;
    }

    public Response build() {
        return new Response(
                status,
                body,
                headers,
                configured,
                fault,
                fromProxy,
                Optional.<ResponseDefinition>absent());
    }
}
