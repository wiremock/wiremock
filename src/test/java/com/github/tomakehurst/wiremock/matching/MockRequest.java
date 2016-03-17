package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.*;

import java.util.Map;
import java.util.Set;

public class MockRequest implements Request {

    private String url;
    private String absoluteUrl;
    private RequestMethod method;

    public static MockRequest mockRequest() {
        return new MockRequest();
    }

    public MockRequest url(String url) {
        this.url = url;
        return this;
    }

    public MockRequest absoluteUrl(String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
        return this;
    }

    public MockRequest method(RequestMethod method) {
        this.method = method;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    @Override
    public RequestMethod getMethod() {
        return method;
    }

    @Override
    public String getHeader(String key) {
        return null;
    }

    @Override
    public HttpHeader header(String key) {
        return null;
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return null;
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }

    @Override
    public boolean containsHeader(String key) {
        return false;
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return null;
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return null;
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }

    @Override
    public String getBodyAsString() {
        return null;
    }

    @Override
    public String getBodyAsBase64() {
        return null;
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return false;
    }
}
