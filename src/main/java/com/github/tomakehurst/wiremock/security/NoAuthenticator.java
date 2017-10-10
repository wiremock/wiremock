package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.Request;

public class NoAuthenticator implements Authenticator {

    @Override
    public boolean authenticate(Request request) {
        return true;
    }
}
