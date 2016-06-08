package com.github.tomakehurst.wiremock.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import org.apache.commons.codec.binary.Base64;

import static com.google.common.base.Charsets.UTF_8;

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
        byte[] usernameAndPassword = (username + ":" + password).getBytes();
        return MultiValuePattern.of(
            WireMock.equalTo(
                "Basic " + new String(Base64.encodeBase64(usernameAndPassword), UTF_8))
        );
    }
}
