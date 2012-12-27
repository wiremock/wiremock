package com.github.tomakehurst.wiremock.client;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class SocketAcceptDelaySpec {

    private int requestCount = 1;
    private final long milliseconds;

    private SocketAcceptDelaySpec(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @JsonCreator
    SocketAcceptDelaySpec(@JsonProperty("milliseconds") long milliseconds, @JsonProperty("requestCount") int requestCount) {
        this.milliseconds = milliseconds;
        this.requestCount = requestCount;
    }

    public static SocketAcceptDelaySpec ofMilliseconds(long milliseconds) {
        return new SocketAcceptDelaySpec(milliseconds);
    }

    public SocketAcceptDelaySpec forNumRequests(int numRequests) {
        this.requestCount = numRequests;
        return this;
    }

    @JsonProperty("requestCount")
    public int requestCount() {
        return requestCount;
    }

    @JsonProperty("milliseconds")
    public long milliseconds() {
        return milliseconds;
    }
}
