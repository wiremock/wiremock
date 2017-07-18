package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.common.ClientError;
import com.github.tomakehurst.wiremock.common.Errors;

public class NotRecordingException extends ClientError {

    public NotRecordingException() {
        super(Errors.notRecording());
    }
}
