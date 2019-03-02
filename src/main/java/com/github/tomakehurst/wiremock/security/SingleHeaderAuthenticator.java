package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;

import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class SingleHeaderAuthenticator implements Authenticator {

    private final String key;
    private final String value;

    public SingleHeaderAuthenticator(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean authenticate(Request request) {
        HttpHeader requestHeader = request.header(key);
        if (requestHeader == null || !requestHeader.isPresent()) {
            return false;
        }

        List<String> headerValues = requestHeader.values();
        return request.containsHeader(AUTHORIZATION) &&
            headerValues.contains(value);
    }
}
