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
package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.util.Collections.list;

public class JettyHttpServletRequestAdapter implements Request {

    private final HttpServletRequest request;
    private byte[] cachedBody;
    private String urlPrefixToRemove;

    public JettyHttpServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    public JettyHttpServletRequestAdapter(HttpServletRequest request, String urlPrefixToRemove) {
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
        if(!isNullOrEmpty(urlPrefixToRemove) && url.startsWith(urlPrefixToRemove)) {
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
                cachedBody = toByteArray(request.getInputStream());
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        return cachedBody;
    }

    @Override
    public String getBodyAsString() {
        return stringFromBytes(getBody());
    }

    @Override
    public String getBodyAsBase64(){
        return Base64.encodeBase64String(getBody());
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getHeader(String key) {
        List<String> headerNames = list(request.getHeaderNames());
        for (String currentKey: headerNames) {
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
        for (String currentKey: headerNames) {
            if (currentKey.toLowerCase().equals(key.toLowerCase())) {
                List<String> valueList = list(request.getHeaders(currentKey));
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
        for (String key: getAllHeaderKeys()) {
            headerList.add(header(key));
        }

        return new HttpHeaders(headerList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getAllHeaderKeys() {
        LinkedHashSet<String> headerKeys = new LinkedHashSet<String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            headerKeys.add(headerNames.nextElement());
        }

        return headerKeys;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        ImmutableMap.Builder<String, Cookie> builder = ImmutableMap.builder();

        for (javax.servlet.http.Cookie cookie:
                Optional.fromNullable(request.getCookies())
                        .or(new javax.servlet.http.Cookie[0])) {
            builder.put(cookie.getName(), convertCookie(cookie));
        }

        return builder.build();
    }

    private static Cookie convertCookie(javax.servlet.http.Cookie servletCookie) {
        return new Cookie(servletCookie.getValue());
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return Optional.fromNullable(splitQuery(request.getQueryString())
                .get(key))
                .or(QueryParameter.absent(key));
    }

    @Override
    public boolean isBrowserProxyRequest() {
        if (request instanceof org.eclipse.jetty.server.Request) {
            org.eclipse.jetty.server.Request jettyRequest = (org.eclipse.jetty.server.Request) request;
            return URI.create(jettyRequest.getUri().toString()).isAbsolute();
        }

        return false;
    }

    @Override
    public String toString() {
        return request.toString() + getBodyAsString();
    }
}
