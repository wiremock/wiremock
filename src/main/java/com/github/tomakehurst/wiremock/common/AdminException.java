package com.github.tomakehurst.wiremock.common;

public class AdminException extends RuntimeException {

    public AdminException(String message) {
        super(message);
    }

    public AdminException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdminException(Throwable cause) {
        super(cause);
    }
}
