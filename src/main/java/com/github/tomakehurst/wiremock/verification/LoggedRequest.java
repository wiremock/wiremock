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
import com.github.tomakehurst.wiremock.http.*;
import com.google.common.base.Splitter;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.github.tomakehurst.wiremock.http.HttpHeaders.copyOf;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LoggedRequest implements Request {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private final String url;
	private final String absoluteUrl;
	private final RequestMethod method;
	private final HttpHeaders headers;
    private final Map<String, QueryParameter> queryParams;
	private final byte[] body;
	private final boolean isBrowserProxyRequest;
    private final Date loggedDate;
	
	public static LoggedRequest createFrom(Request request) {
        return new LoggedRequest(request.getUrl(),
                request.getAbsoluteUrl(),
                request.getMethod(),
                copyOf(request.getHeaders()),
                request.getBody(),
                request.isBrowserProxyRequest(),
                new Date());
	}

    public LoggedRequest(String url,
                         String absoluteUrl,
                         RequestMethod method,
                         HttpHeaders headers,
                         byte[] body,
                         boolean isBrowserProxyRequest,
                         Date loggedDate) {

        this.url = url;
        this.absoluteUrl = absoluteUrl;
        this.method = method;
        this.body = body;
        this.headers = headers;
        this.queryParams = splitQuery(URI.create(url));
        this.isBrowserProxyRequest = isBrowserProxyRequest;
        this.loggedDate = loggedDate;
    }

    @JsonCreator
    public LoggedRequest(@JsonProperty("url") String url,
                         @JsonProperty("absoluteUrl") String absoluteUrl,
                         @JsonProperty("method") RequestMethod method,
                         @JsonProperty("headers") HttpHeaders headers,
                         @JsonProperty("body") String body,
                         @JsonProperty("browserProxyRequest") boolean isBrowserProxyRequest,
                         @JsonProperty("loggedDate") Date loggedDate) {
        this(url, absoluteUrl, method, headers, body.getBytes(), isBrowserProxyRequest, loggedDate);
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
        return headers.getContentTypeHeader();
    }

    @Override
	public boolean containsHeader(String key) {
		return getHeader(key) != null;
	}

    @Override
    public byte[] getBody() {
        return body;
    }

	@Override
    @JsonProperty("body")
	public String getBodyAsString() {
		return new String(body);
	}

	@Override
    @JsonIgnore
	public Set<String> getAllHeaderKeys() {
		return headers.keys();
	}

    @Override
    public QueryParameter queryParameter(String key) {
        return queryParams.get(key);
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
        return format(loggedDate);
    }

    private String format(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }
}
