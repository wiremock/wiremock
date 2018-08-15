package com.github.tomakehurst.wiremock.security;

import com.google.common.net.HttpHeaders;

public class TokenAuthenticator extends SingleHeaderAuthenticator {

    public TokenAuthenticator(String token) {
        super(HttpHeaders.AUTHORIZATION, "Token " + token);
    }
}
