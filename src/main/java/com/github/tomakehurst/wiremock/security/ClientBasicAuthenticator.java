package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.http.HttpHeader;

import java.util.List;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.util.Collections.singletonList;

public class ClientBasicAuthenticator implements ClientAuthenticator {

    private final String username;
    private final String password;

    public ClientBasicAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public List<HttpHeader> generateAuthHeaders() {
        BasicCredentials basicCredentials = new BasicCredentials(username, password);
        return singletonList(httpHeader(AUTHORIZATION, basicCredentials.asAuthorizationHeaderValue()));
    }
}
