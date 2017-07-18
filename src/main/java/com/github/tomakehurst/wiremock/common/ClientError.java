package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.recording.NotRecordingException;

public class ClientError extends RuntimeException {

    private final Errors errors;

    public ClientError(Errors errors) {
        this.errors = errors;
    }

    public static ClientError fromErrors(Errors errors) {
        if (errors.first().getCode().equals(30)) {
            return new NotRecordingException();
        }

        return new ClientError(errors);
    }

    public Errors getErrors() {
        return errors;
    }
}
