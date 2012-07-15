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

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static com.github.tomakehurst.wiremock.http.HttpHeaders.copyOf;

public class LoggedRequest implements Request {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private String url;
	private String absoluteUrl;
	private RequestMethod method;
	private HttpHeaders headers = new HttpHeaders();
	private String body;
	private boolean isBrowserProxyRequest;
    private Date loggedDate;
	
	public static LoggedRequest createFrom(Request request) {
		LoggedRequest loggedRequest = new LoggedRequest();
		loggedRequest.url = request.getUrl();
		loggedRequest.absoluteUrl = request.getAbsoluteUrl();
		loggedRequest.method = request.getMethod();
		loggedRequest.body = request.getBodyAsString();
		loggedRequest.headers = copyOf(request.getHeaders());
		
		loggedRequest.isBrowserProxyRequest = request.isBrowserProxyRequest();
        loggedRequest.loggedDate = new Date();
		
		return loggedRequest;
	}

    private LoggedRequest() {
    }

    @JsonCreator
    private LoggedRequest(@JsonProperty("url") String url,
                         @JsonProperty("absoluteUrl") String absoluteUrl,
                         @JsonProperty("method") RequestMethod method,
                         @JsonProperty("headers") HttpHeaders headers,
                         @JsonProperty("body") String body,
                         @JsonProperty("browserProxyRequest") boolean isBrowserProxyRequest,
                         @JsonProperty("loggedDate") Date loggedDate,
                         @JsonProperty("loggedDateString") String loggedDateString) {

        this.url = url;
        this.absoluteUrl = absoluteUrl;
        this.method = method;
        this.body = body;
        this.headers = headers;
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
        for (String currentKey: headers.keySet()) {
            if (currentKey.toLowerCase().equals(key.toLowerCase())) {
                return headers.getHeader(currentKey);
            }
        }

        return HttpHeader.absent(key);
    }

    @Override
	public boolean containsHeader(String key) {
		return getHeader(key) != null;
	}

	@Override
    @JsonProperty("body")
	public String getBodyAsString() {
		return body;
	}

	@Override
    @JsonIgnore
	public Set<String> getAllHeaderKeys() {
		return headers.keySet();
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
