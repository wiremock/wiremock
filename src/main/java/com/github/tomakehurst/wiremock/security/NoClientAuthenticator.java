package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.HttpHeader;

import java.util.List;

import static java.util.Collections.emptyList;

public class NoClientAuthenticator implements ClientAuthenticator {

    public static NoClientAuthenticator noClientAuthenticator() {
        return new NoClientAuthenticator();
    }

    @Override
    public List<HttpHeader> generateAuthHeaders() {
        return emptyList();
    }
}
