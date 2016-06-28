package com.github.tomakehurst.wiremock.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.google.common.io.BaseEncoding;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class BasicCredentials {

    public final String username;
    public final String password;

    @JsonCreator
    public BasicCredentials(@JsonProperty("username") String username,
                            @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public boolean present() {
        return username != null && password != null;
    }

    public MultiValuePattern asAuthorizationMultiValuePattern() {
        return MultiValuePattern.of(
            equalTo(asAuthorizationHeaderValue())
        );
    }

    public String asAuthorizationHeaderValue() {
        byte[] usernameAndPassword = (username + ":" + password).getBytes();
        return "Basic " + BaseEncoding.base64().encode(usernameAndPassword);
    }
}
