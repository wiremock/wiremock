package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RecordingStatusResult {

    private final RecordingStatus status;

    @JsonCreator
    public RecordingStatusResult(@JsonProperty("status") String status) {
        this(RecordingStatus.valueOf(status));
    }

    public RecordingStatusResult(RecordingStatus status) {
        this.status = status;
    }

    public RecordingStatus getStatus() {
        return status;
    }
}
