package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FixedDelayDistribution implements DelayDistribution {

    private final long milliseconds;

    public FixedDelayDistribution(@JsonProperty("milliseconds") long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public long sampleMillis() {
        return milliseconds;
    }
}
