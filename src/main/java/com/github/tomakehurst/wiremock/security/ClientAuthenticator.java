package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.HttpHeader;

import java.util.List;

public interface ClientAuthenticator {

    List<HttpHeader> generateAuthHeaders();
}
