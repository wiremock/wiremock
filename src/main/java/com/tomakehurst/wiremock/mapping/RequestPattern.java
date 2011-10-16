package com.tomakehurst.wiremock.mapping;

import java.util.Map;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;


public class RequestPattern {

	private String uriPattern;
	private RequestMethod method;
	private HttpHeaders headers;
	
	
	public RequestPattern(RequestMethod method, String uriExpression, HttpHeaders headers) {
		this.uriPattern = uriExpression;
		this.method = method;
		this.headers = headers;
	}
	
	public RequestPattern(RequestMethod method, String uriExpression) {
		this(method, uriExpression, new HttpHeaders());
	}
	
	public RequestPattern() {
		this(null, null, new HttpHeaders());
	}
	
	public boolean isMatchedBy(Request request) {
		return (request.getMethod() == method &&
				request.getUri().equals(uriPattern) && 
				allSpecifiedHeadersArePresentAndWithMatchingValues(request));
	}
	
	private boolean allSpecifiedHeadersArePresentAndWithMatchingValues(Request request) {
		for (Map.Entry<String, String> header: headers.entrySet()) {
			if (!request.containsHeader(header.getKey()) || !request.getHeader(header.getKey()).equals(header.getValue())) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result
				+ ((uriPattern == null) ? 0 : uriPattern.hashCode());
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
		if (uriPattern == null) {
			if (other.uriPattern != null)
				return false;
		} else if (!uriPattern.equals(other.uriPattern))
			return false;
		return true;
	}

	public void setUriPattern(String uriPattern) {
		this.uriPattern = uriPattern;
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

	@Override
	public String toString() {
		return "RequestPattern [uriPattern=" + uriPattern + ", method="
				+ method + "]";
	}
}
