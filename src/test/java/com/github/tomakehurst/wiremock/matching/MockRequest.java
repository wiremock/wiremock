package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Predicate;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

public class MockRequest implements Request {

    private String url = "/";
    private RequestMethod method = RequestMethod.ANY;
    private HttpHeaders headers = new HttpHeaders();

    public static MockRequest mockRequest() {
        return new MockRequest();
    }

    public MockRequest url(String url) {
        this.url = url;
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
        return "http://my.domain" + url;
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
        return ContentTypeHeader.absent();
    }

    @Override
    public HttpHeaders getHeaders() {
        return new HttpHeaders();
    }

    @Override
    public boolean containsHeader(String key) {
        return false;
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        return getHeaders().keys();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return Collections.emptyMap();
    }

    @Override
    public QueryParameter queryParameter(String key) {
        Map<String, QueryParameter> queryParams = Urls.splitQuery(URI.create(url));
        return queryParams.get(key);
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }

    @Override
    public String getBodyAsString() {
        return "";
    }

    @Override
    public String getBodyAsBase64() {
        return "";
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
