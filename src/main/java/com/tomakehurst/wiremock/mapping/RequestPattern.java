package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.http.RequestMethod;


public class RequestPattern {

	private String uriPattern;
	private RequestMethod method;
	
	
	public RequestPattern(RequestMethod method, String uriExpression) {
		this.uriPattern = uriExpression;
		this.method = method;
	}
	
	public RequestPattern() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

	public boolean isMatchedBy(Request request) {
		return (request.getMethod() == method && request.getUri().equals(uriPattern));
	}
	
	@Override
	public String toString() {
		return "RequestPattern [uriPattern=" + uriPattern + ", method="
				+ method + "]";
	}
}
