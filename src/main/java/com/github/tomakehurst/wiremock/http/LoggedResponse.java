package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoggedResponse {

    private final int status;
    private final HttpHeaders headers;
    private final String body;
    private final byte[] bodyBytes;
    private final Fault fault;

    public LoggedResponse(@JsonProperty("status") int status,
                          @JsonProperty("headers") HttpHeaders headers,
                          @JsonProperty("body") String body,
                          @JsonProperty("bodyBytes") byte[] bodyBytes,
                          @JsonProperty("fault") Fault fault) {
        this.status = status;
        this.headers = headers;
        this.bodyBytes = bodyBytes;
        this.body = body;
        this.fault = fault;
    }

    public static LoggedResponse from(Response response) {
        return new LoggedResponse(
            response.getStatus(),
            response.getHeaders() == null || response.getHeaders().all().isEmpty() ? null : response.getHeaders(),
            response.getBody() != null ? response.getBodyAsString() : null,
            response.getBody(),
            response.getFault()
        );
    }

    public int getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public String getBody() {
        return body;
    }

    public Fault getFault() {
        return fault;
    }
}
