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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;

@JsonSerialize(include=Inclusion.NON_NULL)
public class RequestPattern {

    private String urlPattern;
	private String url;
    private String urlPath;
	private String urlPathPattern;
    private RequestMethod method;
    private Map<String, ValuePattern> headerPatterns;
    private Map<String, ValuePattern> queryParamPatterns;
    private List<ValuePattern> bodyPatterns;

    public RequestPattern(RequestMethod method, String url, Map<String, ValuePattern> headerPatterns, Map<String, ValuePattern> queryParamPatterns) {
        this.url = url;
        this.method = method;
        this.headerPatterns = headerPatterns;
        this.queryParamPatterns = queryParamPatterns;
    }

    public RequestPattern(RequestMethod method, String url, Map<String, ValuePattern> headerPatterns) {
		this.url = url;
		this.method = method;
		this.headerPatterns = headerPatterns;
	}

	public RequestPattern(RequestMethod method) {
		this.method = method;
	}

	public RequestPattern(RequestMethod method, String url) {
		this.url = url;
		this.method = method;
	}

	public RequestPattern() {
	}

    public static RequestPattern everything() {
        RequestPattern requestPattern = new RequestPattern(RequestMethod.ANY);
        requestPattern.setUrlPattern(".*");
        return requestPattern;
    }

    public static RequestPattern buildRequestPatternFrom(String json) {
        return Json.read(json, RequestPattern.class);
    }

    private void assertIsInValidState() {
        if (from(asList(url, urlPath, urlPattern, urlPathPattern)).filter(notNull()).size() > 1) {
			throw new IllegalStateException("Only one of url, urlPattern, urlPath or urlPathPattern may be set");
		}
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
		assertIsInValidState();
	}

	public RequestMethod getMethod() {
		return method;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public Map<String, ValuePattern> getHeaders() {
		return headerPatterns;
	}

    public Map<String, ValuePattern> getQueryParameters() {
        return queryParamPatterns;
    }

    public void setQueryParameters(Map<String, ValuePattern> queryParamPatterns) {
        this.queryParamPatterns = queryParamPatterns;
    }

    public void addHeader(String key, ValuePattern pattern) {
		if (headerPatterns == null) {
			headerPatterns = newLinkedHashMap();
		}

		headerPatterns.put(key, pattern);
	}

    public void addQueryParam(String key, ValuePattern valuePattern) {
        if (queryParamPatterns == null) {
            queryParamPatterns = newLinkedHashMap();
        }

        queryParamPatterns.put(key, valuePattern);
    }

	public void setHeaders(Map<String, ValuePattern> headers) {
		this.headerPatterns = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		assertIsInValidState();
	}

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
        assertIsInValidState();
    }

	public String getUrlPathPattern() {
		return urlPathPattern;
	}

	public void setUrlPathPattern(String urlPathPattern) {
		this.urlPathPattern = urlPathPattern;
		assertIsInValidState();
	}

	public List<ValuePattern> getBodyPatterns() {
		return bodyPatterns;
	}

	public void setBodyPatterns(List<ValuePattern> bodyPatterns) {
		this.bodyPatterns = bodyPatterns;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequestPattern that = (RequestPattern) o;
		return Objects.equals(urlPattern, that.urlPattern) &&
				Objects.equals(url, that.url) &&
				Objects.equals(urlPath, that.urlPath) &&
				Objects.equals(urlPathPattern, that.urlPathPattern) &&
				Objects.equals(method, that.method) &&
				Objects.equals(headerPatterns, that.headerPatterns) &&
				Objects.equals(queryParamPatterns, that.queryParamPatterns) &&
				Objects.equals(bodyPatterns, that.bodyPatterns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(urlPattern, url, urlPath, urlPathPattern, method, headerPatterns, queryParamPatterns, bodyPatterns);
	}

	@Override
	public String toString() {
		return Json.write(this);
	}
}
