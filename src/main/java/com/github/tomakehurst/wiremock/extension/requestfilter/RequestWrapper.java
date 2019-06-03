package com.github.tomakehurst.wiremock.extension.requestfilter;

import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RequestWrapper implements Request {

    private final Request delegate;

    public RequestWrapper(Request delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getUrl() {
        return delegate.getUrl();
    }

    @Override
    public String getAbsoluteUrl() {
        return delegate.getAbsoluteUrl();
    }

    @Override
    public RequestMethod getMethod() {
        return delegate.getMethod();
    }

    @Override
    public String getScheme() {
        return delegate.getScheme();
    }

    @Override
    public String getHost() {
        return delegate.getHost();
    }

    @Override
    public int getPort() {
        return delegate.getPort();
    }

    @Override
    public String getClientIp() {
        return delegate.getClientIp();
    }

    @Override
    public String getHeader(String key) {
        return delegate.getHeader(key);
    }

    @Override
    public HttpHeader header(String key) {
        return delegate.header(key);
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return delegate.contentTypeHeader();
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public boolean containsHeader(String key) {
        return delegate.containsHeader(key);
    }

    @Override
    public Set<String> getAllHeaderKeys() {
        return delegate.getAllHeaderKeys();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return delegate.getCookies();
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return delegate.queryParameter(key);
    }

    @Override
    public byte[] getBody() {
        return delegate.getBody();
    }

    @Override
    public String getBodyAsString() {
        return delegate.getBodyAsString();
    }

    @Override
    public String getBodyAsBase64() {
        return delegate.getBodyAsBase64();
    }

    @Override
    public boolean isMultipart() {
        return delegate.isMultipart();
    }

    @Override
    public Collection<Part> getParts() {
        return delegate.getParts();
    }

    @Override
    public Part getPart(String name) {
        return delegate.getPart(name);
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return delegate.isBrowserProxyRequest();
    }

    @Override
    public Optional<Request> getOriginalRequest() {
        return delegate.getOriginalRequest();
    }
}
