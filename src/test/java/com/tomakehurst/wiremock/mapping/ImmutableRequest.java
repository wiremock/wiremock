package com.tomakehurst.wiremock.mapping;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;



public class ImmutableRequest implements Request {

	private final String uri;
	private final RequestMethod method;
	private final HttpHeaders headers;
	
	public ImmutableRequest(RequestMethod method, String uri, HttpHeaders headers) {
		this.uri = uri;
		this.method = method;
		this.headers = headers;
	}
	
	public ImmutableRequest(RequestMethod method, String uri) {
		this(method, uri, new HttpHeaders());
	}

	/* (non-Javadoc)
	 * @see com.tomakehurst.wiremock.mapping.Request#getUri()
	 */
	@Override
	public String getUri() {
		return uri;
	}

	/* (non-Javadoc)
	 * @see com.tomakehurst.wiremock.mapping.Request#getMethod()
	 */
	@Override
	public RequestMethod getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		ImmutableRequest other = (ImmutableRequest) obj;
		if (method != other.method)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String getBodyAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeader(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsHeader(String key) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
