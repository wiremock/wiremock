package com.github.tomakehurst.wiremock.client;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class RequestDelaySpec {

    private final int milliseconds;

    @JsonCreator
    public RequestDelaySpec(@JsonProperty("milliseconds") int milliseconds) {
        this.milliseconds = milliseconds;
    }

    @JsonProperty("milliseconds")
    public int milliseconds() {
        return milliseconds;
    }
}
