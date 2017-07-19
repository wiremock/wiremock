package com.github.tomakehurst.wiremock.common;

public class InvalidRequestException extends ClientError {

    public InvalidRequestException(Errors errors) {
        super(errors);
    }
}
