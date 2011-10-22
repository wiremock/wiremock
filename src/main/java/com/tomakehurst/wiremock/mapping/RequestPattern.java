package com.tomakehurst.wiremock.mapping;

import java.util.Map;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;


public class RequestPattern {

	private String urlPattern;
	private String url;
	private RequestMethod method;
	private HttpHeaders headers;
	
	
	public RequestPattern(RequestMethod method, String url, HttpHeaders headers) {
		this.url = url;
		this.method = method;
		this.headers = headers;
	}
	
	public RequestPattern(RequestMethod method) {
		this(method, null, new HttpHeaders());
	}
	
	public RequestPattern(RequestMethod method, String url) {
		this(method, url, new HttpHeaders());
	}
	
	public RequestPattern() {
		this(null, null, new HttpHeaders());
	}
	
	private void assertIsInValidState() {
		if (url != null && urlPattern != null) {
			throw new IllegalStateException("URL and URL pattern may not be set simultaneously");
		}
	}
	
	public boolean isMatchedBy(Request request) {
		return (request.getMethod() == method &&
				urlMatchedBy(request.getUrl()) && 
				allSpecifiedHeadersArePresentAndWithMatchingValues(request));
	}
	
	private boolean urlMatchedBy(String candidateUrl) {
		if (urlPattern == null) {
			return url.equals(candidateUrl);
		}
		
		return candidateUrl.matches(urlPattern);
	}
	
	private boolean allSpecifiedHeadersArePresentAndWithMatchingValues(Request request) {
		for (Map.Entry<String, String> header: headers.entrySet()) {
			if (!request.containsHeader(header.getKey()) || !request.getHeader(header.getKey()).equals(header.getValue())) {
				return false;
			}
		}
		
		return true;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
		assertIsInValidState();
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		assertIsInValidState();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

	@Override
	public String toString() {
		return "RequestPattern [urlPattern=" + urlPattern + ", url=" + url
				+ ", method=" + method + ", headers=" + headers + "]";
	}
}
