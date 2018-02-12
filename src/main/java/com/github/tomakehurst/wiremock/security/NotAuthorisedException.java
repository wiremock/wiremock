package com.github.tomakehurst.wiremock.security;

public class NotAuthorisedException extends RuntimeException {

    public NotAuthorisedException() {
    }

    public NotAuthorisedException(String message) {
        super(message);
    }

    public NotAuthorisedException(String message, Throwable cause) {
        super(message, cause);
    }
}
