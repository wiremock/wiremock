package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.recording.NotRecordingException;

public class ClientError extends RuntimeException {

    private final Errors errors;

    public ClientError(Errors errors) {
        this.errors = errors;
    }

    public static ClientError fromErrors(Errors errors) {
        Integer errorCode = errors.first().getCode();
        switch (errorCode) {
            case 10:
                return new InvalidRequestException(errors);
            case 30:
                return new NotRecordingException();
            default:
                return new ClientError(errors);
        }
    }

    public Errors getErrors() {
        return errors;
    }
}
