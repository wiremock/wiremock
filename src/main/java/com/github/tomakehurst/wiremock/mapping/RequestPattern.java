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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.regex.Pattern.DOTALL;

import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.github.tomakehurst.wiremock.http.RequestMethod;

@JsonSerialize(include=Inclusion.NON_NULL)
public class RequestPattern {

	private String urlPattern;
	private String url;
	private RequestMethod method;
	private Map<String, HeaderPattern> headers;
	private String bodyPattern;
	
	public RequestPattern(RequestMethod method, String url, Map<String, HeaderPattern> headers) {
		this.url = url;
		this.method = method;
		this.headers = headers;
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
	
	private void assertIsInValidState() {
		if (url != null && urlPattern != null) {
			throw new IllegalStateException("URL and URL pattern may not be set simultaneously");
		}
	}
	
	public boolean isMatchedBy(Request request) {
		return (urlIsMatch(request) &&
				methodMatches(request) &&
				headersMatch(request) &&
				bodyMatches(request));
	}
	
	private boolean urlIsMatch(Request request) {
		String candidateUrl = request.getUrl();
		boolean matched;
		if (urlPattern == null) {
			matched = url.equals(candidateUrl);
		} else {
			matched = candidateUrl.matches(urlPattern);
		}
		
		return matched;
	}
	
	private boolean methodMatches(Request request) {
		boolean matched = method == ANY || request.getMethod() == method;
		if (!matched) {
			notifier().info(String.format("URL %s is match, but method %s is not", request.getUrl(), request.getMethod()));
		}
		
		return matched;
	}
	
	private boolean headersMatch(Request request) {
		if (headers == null) {
			return true;
		}

		for (Map.Entry<String, HeaderPattern> header: headers.entrySet()) {
			HeaderPattern headerPattern = header.getValue();
			String key = header.getKey();
			if (!request.containsHeader(key) || !headerPattern.isMatchFor(request.getHeader(key))) {
				notifier().info(String.format("URL %s is match, but header %s is not", request.getUrl(), key));
				return false;
			}
		}
		
		return true;
	}
	
	private boolean bodyMatches(Request request) {
		if (bodyPattern == null) {
			return true;
		}
		
		Pattern pattern = Pattern.compile(bodyPattern, DOTALL);
		boolean matches = pattern.matcher(request.getBodyAsString()).matches();
		
		if (!matches) {
			notifier().info(String.format("URL %s is match, but body is not: %s", request.getUrl(), request.getBodyAsString()));
		}
		
		return matches;
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

	public Map<String, HeaderPattern> getHeaders() {
		return headers;
	}
	
	public void addHeader(String key, HeaderPattern pattern) {
		if (headers == null) {
			headers = newLinkedHashMap();
		}
		
		headers.put(key, pattern);
	}
	
	public void setHeaders(Map<String, HeaderPattern> headers) {
		this.headers = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		assertIsInValidState();
	}
	
	public String getBodyPattern() {
		return bodyPattern;
	}

	public void setBodyPattern(String bodyPattern) {
		this.bodyPattern = bodyPattern;
	}

	

	@Override
	public String toString() {
		return "RequestPattern [urlPattern=" + urlPattern + ", url=" + url
				+ ", method=" + method + ", headers=" + headers
				+ ", bodyPattern=" + bodyPattern + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bodyPattern == null) ? 0 : bodyPattern.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result
				+ ((urlPattern == null) ? 0 : urlPattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestPattern other = (RequestPattern) obj;
		if (bodyPattern == null) {
			if (other.bodyPattern != null)
				return false;
		} else if (!bodyPattern.equals(other.bodyPattern))
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (method != other.method)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (urlPattern == null) {
			if (other.urlPattern != null)
				return false;
		} else if (!urlPattern.equals(other.urlPattern))
			return false;
		return true;
	}

	
}
