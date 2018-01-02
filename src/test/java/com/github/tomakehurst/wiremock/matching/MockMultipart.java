package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MockMultipart implements Request.Part {

    private String name;
    private List<HttpHeader> headers = newArrayList();
    private Body body;

    public static MockMultipart mockPart() {
        return new MockMultipart();
    }

    public MockMultipart name(String name) {
        this.name = name;
        return this;
    }

    public MockMultipart headers(List<HttpHeader> headers) {
        this.headers = headers;
        return this;
    }

    public MockMultipart header(String key, String... values) {
        headers.add(new HttpHeader(key, values));
        return this;
    }

    public MockMultipart body(String body) {
        this.body = new Body(body);
        return this;
    }

    public MockMultipart body(byte[] body) {
        this.body = new Body(body);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HttpHeader getHeader(String key) {
        return getHeaders().getHeader(key);
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders(headers);
    }

    @Override
    public Body getBody() {
        return body;
    }
}
