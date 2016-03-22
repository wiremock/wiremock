package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

public class MockRequest implements Request {

    private String url;
    private String absoluteUrl;
    private RequestMethod method;
    private HttpHeaders headers = new HttpHeaders();

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
        return header(key).firstValue();
    }

    @Override
    public HttpHeader header(final String key) {
        return tryFind(headers.all(), new Predicate<HttpHeader>() {
            public boolean apply(HttpHeader input) {
                return input.keyEquals(key);
            }
        }).or(HttpHeader.absent(key));
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return null;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
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

    public MockRequest header(String key, String value) {
        headers = headers.plus(httpHeader(key, value));
        return this;
    }
}
