/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.jetty9.JettyUtils;
import com.google.common.collect.ImmutableMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import java.nio.charset.Charset;
import static com.google.common.base.Charsets.UTF_8;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.util.Collections.list;

public class WireMockHttpServletRequestAdapter implements Request {

    private final HttpServletRequest request;
    private byte[] cachedBody;
    private String urlPrefixToRemove;

    public WireMockHttpServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    public WireMockHttpServletRequestAdapter(HttpServletRequest request, String urlPrefixToRemove) {
        this.request = request;
        this.urlPrefixToRemove = urlPrefixToRemove;
    }

    @Override
    public String getUrl() {
        String url = request.getRequestURI();

        String contextPath = request.getContextPath();
        if (!isNullOrEmpty(contextPath) && url.startsWith(contextPath)) {
            url = url.substring(contextPath.length());
        }
        if (!isNullOrEmpty(urlPrefixToRemove) && url.startsWith(urlPrefixToRemove)) {
            url = url.substring(urlPrefixToRemove.length());
        }

        return withQueryStringIfPresent(url);
    }

    @Override
    public String getAbsoluteUrl() {
        return withQueryStringIfPresent(request.getRequestURL().toString());
    }

    private String withQueryStringIfPresent(String url) {
        return url + (isNullOrEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
    }

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.fromString(request.getMethod().toUpperCase());
    }

    @Override
    public String getClientIp() {
        String forwardedForHeader = this.getHeader("X-Forwarded-For");

        if (forwardedForHeader != null && forwardedForHeader.length() > 0) {
            return forwardedForHeader;
        }

        return  request.getRemoteAddr();
    }

    @Override
    public byte[] getBody() {
        if (cachedBody == null) {
            try {
                byte[] body = toByteArray(request.getInputStream());
                boolean isGzipped = hasGzipEncoding() || Gzip.isGzipped(body);
                cachedBody = isGzipped ? Gzip.unGzip(body) : body;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        return cachedBody;
    }

    private Charset encodingFromContentTypeHeaderOrUtf8() {
        ContentTypeHeader contentTypeHeader = contentTypeHeader();
        if (contentTypeHeader != null) {
            return contentTypeHeader.charset();
        } 
        return UTF_8;
    }

    private boolean hasGzipEncoding() {
        String encodingHeader = request.getHeader("Content-Encoding");
        return encodingHeader != null && encodingHeader.contains("gzip");
    }

    @Override
    public String getBodyAsString() {
        return stringFromBytes(getBody(), encodingFromContentTypeHeaderOrUtf8());
    }

    @Override
    public String getBodyAsBase64() {
        return encodeBase64(getBody());
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getHeader(String key) {
        List<String> headerNames = list(request.getHeaderNames());
        for (String currentKey : headerNames) {
            if (currentKey.toLowerCase().equals(key.toLowerCase())) {
                return request.getHeader(currentKey);
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpHeader header(String key) {
        List<String> headerNames = list(request.getHeaderNames());
        for (String currentKey : headerNames) {
            if (currentKey.toLowerCase().equals(key.toLowerCase())) {
                List<String> valueList = list(request.getHeaders(currentKey));
                if (valueList.isEmpty()) {
                    return HttpHeader.empty(key);
                }

                return new HttpHeader(key, valueList);
            }
        }

        return HttpHeader.absent(key);
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        return getHeaders().getContentTypeHeader();
    }

    @Override
    public boolean containsHeader(String key) {
        return header(key).isPresent();
    }

    @Override
    public HttpHeaders getHeaders() {
        List<HttpHeader> headerList = newArrayList();
        for (String key : getAllHeaderKeys()) {
            headerList.add(header(key));
        }

        return new HttpHeaders(headerList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getAllHeaderKeys() {
        LinkedHashSet<String> headerKeys = new LinkedHashSet<String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            headerKeys.add(headerNames.nextElement());
        }

        return headerKeys;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        ImmutableMap.Builder<String, Cookie> builder = ImmutableMap.builder();

        for (javax.servlet.http.Cookie cookie :
            firstNonNull(request.getCookies(), new javax.servlet.http.Cookie[0])) {
            builder.put(cookie.getName(), convertCookie(cookie));
        }

        return builder.build();
    }

    private static Cookie convertCookie(javax.servlet.http.Cookie servletCookie) {
        return new Cookie(servletCookie.getValue());
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return firstNonNull((splitQuery(request.getQueryString())
                .get(key)),
            QueryParameter.absent(key));
    }

    @Override
    public boolean isBrowserProxyRequest() {
        if (!isJetty()) {
            return false;
        }
        if (request instanceof org.eclipse.jetty.server.Request) {
            org.eclipse.jetty.server.Request jettyRequest = (org.eclipse.jetty.server.Request) request;
            return JettyUtils.getUri(jettyRequest).isAbsolute();
        }

        return false;
    }

    private boolean isJetty() {
        try {
            getClass("org.eclipse.jetty.server.Request");
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    private void getClass(String type) throws ClassNotFoundException {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = contextCL == null ? WireMockHttpServletRequestAdapter.class.getClassLoader() : contextCL;
        Class.forName(type, false, loader);
    }

    @Override
    public String toString() {
        return request.toString() + getBodyAsString();
    }
}
