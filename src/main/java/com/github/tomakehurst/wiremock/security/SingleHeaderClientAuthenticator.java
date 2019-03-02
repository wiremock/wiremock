package com.github.tomakehurst.wiremock.security;

import com.github.tomakehurst.wiremock.http.HttpHeader;

import java.util.Collections;
import java.util.List;

public class SingleHeaderClientAuthenticator implements ClientAuthenticator {

    private final String key;
    private final String value;

    public SingleHeaderClientAuthenticator(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public List<HttpHeader> generateAuthHeaders() {
        return Collections.singletonList(new HttpHeader(key, value));
    }
}
