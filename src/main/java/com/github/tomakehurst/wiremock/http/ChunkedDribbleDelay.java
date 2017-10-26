package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChunkedDribbleDelay {

    private final Integer numberOfChunks;
    private final Integer totalDuration;

    @JsonCreator
    public ChunkedDribbleDelay(@JsonProperty("numberOfChunks") Integer numberOfChunks, @JsonProperty("totalDuration") Integer totalDuration) {
        this.numberOfChunks = numberOfChunks;
        this.totalDuration = totalDuration;
    }

    public Integer getNumberOfChunks() {
        return numberOfChunks;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }
}
