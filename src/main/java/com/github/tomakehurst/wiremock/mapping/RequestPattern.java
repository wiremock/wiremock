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

import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.mapping.ValuePattern.matching;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Maps.newLinkedHashMap;

@JsonSerialize(include=Inclusion.NON_NULL)
public class RequestPattern {

	private String urlPattern;
	private String url;
	private RequestMethod method;
	private Map<String, ValuePattern> headerPatterns;
	private List<ValuePattern> bodyPatterns;
	
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
		if (headerPatterns == null) {
			return true;
		}

		for (Map.Entry<String, ValuePattern> headerPattern: headerPatterns.entrySet()) {
			ValuePattern headerValuePattern = headerPattern.getValue();
			String key = headerPattern.getKey();
			if (!request.containsHeader(key) || !request.header(key).hasValueMatching(headerValuePattern)) {
				notifier().info(String.format(
                        "URL %s is match, but header %s is not. For a match, value should %s",
                        request.getUrl(),
                        key,
                        headerValuePattern.toString()));
				return false;
			}
		}
		
		return true;
	}
	
	private boolean bodyMatches(Request request) {
		if (bodyPatterns == null) {
			return true;
		}
		
		boolean matches = all(bodyPatterns, matching(request.getBodyAsString()));
		
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

	public Map<String, ValuePattern> getHeaders() {
		return headerPatterns;
	}
	
	public void addHeader(String key, ValuePattern pattern) {
		if (headerPatterns == null) {
			headerPatterns = newLinkedHashMap();
		}
		
		headerPatterns.put(key, pattern);
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
	
	public List<ValuePattern> getBodyPatterns() {
		return bodyPatterns;
	}

	public void setBodyPatterns(List<ValuePattern> bodyPatterns) {
		this.bodyPatterns = bodyPatterns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bodyPatterns == null) ? 0 : bodyPatterns.hashCode());
		result = prime * result + ((headerPatterns == null) ? 0 : headerPatterns.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result
				+ ((urlPattern == null) ? 0 : urlPattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RequestPattern other = (RequestPattern) obj;
		if (bodyPatterns == null) {
			if (other.bodyPatterns != null) {
				return false;
			}
		} else if (!bodyPatterns.equals(other.bodyPatterns)) {
			return false;
		}
		if (headerPatterns == null) {
			if (other.headerPatterns != null) {
				return false;
			}
		} else if (!headerPatterns.equals(other.headerPatterns)) {
			return false;
		}
		if (method != other.method) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		if (urlPattern == null) {
			if (other.urlPattern != null) {
				return false;
			}
		} else if (!urlPattern.equals(other.urlPattern)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return JsonMappingBinder.write(this);
	}
}
