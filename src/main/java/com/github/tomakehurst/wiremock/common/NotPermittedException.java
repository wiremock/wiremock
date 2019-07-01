package com.github.tomakehurst.wiremock.common;

public class NotPermittedException extends ClientError {

    public NotPermittedException(Errors errors) {
        super(errors);
    }
}
