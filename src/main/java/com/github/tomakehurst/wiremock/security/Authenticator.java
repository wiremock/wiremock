package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.Request;

public interface Authenticator {

    boolean authenticate(Request request);
}
