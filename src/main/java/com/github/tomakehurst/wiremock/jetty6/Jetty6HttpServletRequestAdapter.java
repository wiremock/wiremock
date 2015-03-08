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
package com.github.tomakehurst.wiremock.jetty6;

import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.util.Collections.list;

public class Jetty6HttpServletRequestAdapter implements Request {
    
    private final HttpServletRequest request;
    private byte[] cachedBody;
    private String urlPrefixToRemove;

    public Jetty6HttpServletRequestAdapter(HttpServletRequest request) {
        this.request = request;
    }

    public Jetty6HttpServletRequestAdapter(HttpServletRequest request, String urlPrefixToRemove) {
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
        return RequestMethod.valueOf(request.getMethod().toUpperCase());
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
        byte[] body = getBody();
        return new String(body, UTF_8);
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
    public QueryParameter queryParameter(String key) {
		return Optional.fromNullable(splitQuery(request.getQueryString())
				.get(key))
				.or(QueryParameter.absent(key));
    }

    @Override
    public boolean isBrowserProxyRequest() {
        if (request instanceof org.mortbay.jetty.Request) {
            org.mortbay.jetty.Request jettyRequest = (org.mortbay.jetty.Request) request;
            URI uri = URI.create(jettyRequest.getUri().toString());
            return uri.isAbsolute();
        }

        return false;
    }

    @Override
    public String toString() {
        return request.toString() + getBodyAsString();
    }
}
