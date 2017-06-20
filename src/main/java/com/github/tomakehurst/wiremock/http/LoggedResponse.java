package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Encoding;

import java.nio.charset.*;

public class LoggedResponse {

    private final int status;
    private final HttpHeaders headers;
    private final byte[] body;
    private final Fault fault;

    public LoggedResponse(@JsonProperty("status") int status,
                          @JsonProperty("headers") HttpHeaders headers,
                          @JsonProperty("bodyAsBase64") String bodyAsBase64,
                          @JsonProperty("fault") Fault fault,
                          @JsonProperty("body") String ignoredBodyOnlyUsedForBinding) {
        this.status = status;
        this.headers = headers;
        this.body = Encoding.decodeBase64(bodyAsBase64);
        this.fault = fault;
    }

    public static LoggedResponse from(Response response) {
        return new LoggedResponse(
            response.getStatus(),
            response.getHeaders() == null || response.getHeaders().all().isEmpty() ? null : response.getHeaders(),
            Encoding.encodeBase64(response.getBody()),
            response.getFault(),
            null
        );
    }

    public int getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Retrieve body as a String encoded in the charset in the "Content-Type" header, or, if that's not present, UTF-8.
     *
     * @return String encoded using detected charset or UTF-8
     */
    @JsonProperty("body")
    public String getBodyAsString() {
        if (body == null) {
            return "";
        }

        Charset charset = (headers == null) ? StandardCharsets.UTF_8 : headers.getContentTypeHeader().encodingOrUtf8();
        return new String(body, charset);
    }

    @JsonIgnore
    public byte[] getBody() {
        return body;
    }

    @JsonProperty("bodyAsBase64")
    public String getBodyAsBase64() {
        return Encoding.encodeBase64(body);
    }

    public Fault getFault() {
        return fault;
    }
}
