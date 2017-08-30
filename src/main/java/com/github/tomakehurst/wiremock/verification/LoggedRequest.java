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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Dates;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import java.nio.charset.Charset;
import static com.google.common.base.Charsets.UTF_8;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.github.tomakehurst.wiremock.http.HttpHeaders.copyOf;
import static com.google.common.base.MoreObjects.firstNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggedRequest implements Request {

    private final String url;
    private final String absoluteUrl;
    private final String clientIp;
    private final RequestMethod method;
    private final HttpHeaders headers;
    private final Map<String, Cookie> cookies;
    private final Map<String, QueryParameter> queryParams;
    private final byte[] body;
    private final boolean isBrowserProxyRequest;
    private final Date loggedDate;

    public static LoggedRequest createFrom(Request request) {
        return new LoggedRequest(request.getUrl(),
            request.getAbsoluteUrl(),
            request.getMethod(),
            request.getClientIp(),
            copyOf(request.getHeaders()),
            ImmutableMap.copyOf(request.getCookies()),
            request.isBrowserProxyRequest(),
            new Date(),
            request.getBodyAsBase64(),
            null);
    }

    @JsonCreator
    public LoggedRequest(
            @JsonProperty("url") String url,
            @JsonProperty("absoluteUrl") String absoluteUrl,
            @JsonProperty("method") RequestMethod method,
            @JsonProperty("clientIp") String clientIp,
            @JsonProperty("headers") HttpHeaders headers,
            @JsonProperty("cookies") Map<String, Cookie> cookies,
            @JsonProperty("browserProxyRequest") boolean isBrowserProxyRequest,
            @JsonProperty("loggedDate") Date loggedDate,
            @JsonProperty("bodyAsBase64") String bodyAsBase64,
            @JsonProperty("body") String ignoredBodyOnlyUsedForBinding) {
        this.url = url;
        this.absoluteUrl = absoluteUrl;
        this.clientIp = clientIp;
        this.method = method;
        this.body = decodeBase64(bodyAsBase64);
        this.headers = headers;
        this.cookies = cookies;
        this.queryParams = splitQuery(URI.create(url));
        this.isBrowserProxyRequest = isBrowserProxyRequest;
        this.loggedDate = loggedDate;
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
    public String getClientIp() {
        return clientIp;
    }

    @Override
    @JsonIgnore
    public String getHeader(String key) {
        HttpHeader header = header(key);
        if (header.isPresent()) {
            return header.firstValue();
        }

        return null;
    }

    @Override
    public HttpHeader header(String key) {
        return headers.getHeader(key);
    }

    @Override
    public ContentTypeHeader contentTypeHeader() {
        if (headers != null) {
            return headers.getContentTypeHeader();
        } 
        return null;
    }

    private Charset encodingFromContentTypeHeaderOrUtf8() {
        ContentTypeHeader contentTypeHeader = contentTypeHeader();
        if (contentTypeHeader != null) {
            return contentTypeHeader.charset();
        }
        return UTF_8;
    }

    @Override
    public boolean containsHeader(String key) {
        return getHeader(key) != null;
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    @JsonProperty("body")
    public String getBodyAsString() {
        return stringFromBytes(body, encodingFromContentTypeHeaderOrUtf8());
    }

    @Override
    @JsonProperty("bodyAsBase64")
    public String getBodyAsBase64() {
        return encodeBase64(body);
    }

    @Override
    @JsonIgnore
    public Set<String> getAllHeaderKeys() {
        return headers.keys();
    }

    @Override
    public QueryParameter queryParameter(String key) {
        return firstNonNull(queryParams.get(key), QueryParameter.absent(key));
    }

    @JsonProperty("queryParams")
    public Map<String, QueryParameter> getQueryParams() {
        return queryParams;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public boolean isBrowserProxyRequest() {
        return isBrowserProxyRequest;
    }

    public Date getLoggedDate() {
        return loggedDate;
    }

    public String getLoggedDateString() {
        return Dates.format(loggedDate);
    }

    @Override
    public String toString() {
        return Json.write(this);
    }
}
