package com.github.tomakehurst.wiremock.http;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

public class GenericHttpUriRequest extends HttpRequestBase {

    private final String methodName;

    public GenericHttpUriRequest(String methodName, String url) {
        this.methodName = methodName;
        setURI(URI.create(url));
    }

    @Override
    public String getMethod() {
        return methodName;
    }
}
