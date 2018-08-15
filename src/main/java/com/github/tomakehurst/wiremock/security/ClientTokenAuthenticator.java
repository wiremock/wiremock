package com.github.tomakehurst.wiremock.security;

import com.google.common.net.HttpHeaders;

public class ClientTokenAuthenticator extends SingleHeaderClientAuthenticator {

    public ClientTokenAuthenticator(String token) {
        super(HttpHeaders.AUTHORIZATION, "Token " + token);
    }
}
