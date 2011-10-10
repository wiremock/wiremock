package com.tomakehurst.wiremock.mapping;


public class RequestResponseMapping {

	private final RequestPattern requestPattern;
	private final Response response;
	
	public RequestResponseMapping(RequestPattern requestPattern, Response response) {
		this.requestPattern = requestPattern;
		this.response = response;
	}
	
	public RequestPattern getRequestPattern() {
		return requestPattern;
	}
	
	public Response getResponse() {
		return response;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((requestPattern == null) ? 0 : requestPattern.hashCode());
		result = prime * result
				+ ((response == null) ? 0 : response.hashCode());
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
		RequestResponseMapping other = (RequestResponseMapping) obj;
		if (requestPattern == null) {
			if (other.requestPattern != null)
				return false;
		} else if (!requestPattern.equals(other.requestPattern))
			return false;
		if (response == null) {
			if (other.response != null)
				return false;
		} else if (!response.equals(other.response))
			return false;
		return true;
	}
}
