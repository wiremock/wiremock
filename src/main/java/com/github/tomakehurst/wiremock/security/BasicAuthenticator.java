package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.List;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.util.Arrays.asList;

public class BasicAuthenticator implements Authenticator {

    private final List<BasicCredentials> credentials;

    public BasicAuthenticator(List<BasicCredentials> credentials) {
        this.credentials = credentials;
    }

    public BasicAuthenticator(BasicCredentials... credentials) {
        this.credentials = asList(credentials);
    }

    public BasicAuthenticator(String username, String password) {
        this(new BasicCredentials(username, password));
    }

    @Override
    public boolean authenticate(Request request) {
        List<String> headerValues = FluentIterable.from(credentials).transform(new Function<BasicCredentials, String>() {
            @Override
            public String apply(BasicCredentials input) {
                return input.asAuthorizationHeaderValue();
            }
        }).toList();
        return request.containsHeader(AUTHORIZATION) &&
               headerValues.contains(request.header(AUTHORIZATION).firstValue());
    }
}
