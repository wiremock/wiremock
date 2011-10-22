package com.tomakehurst.wiremock.mapping;


public class RequestResponseMapping {

	private RequestPattern request;
	private Response response;
	
	public RequestResponseMapping(RequestPattern requestPattern, Response response) {
		this.request = requestPattern;
		this.response = response;
	}
	
	public RequestResponseMapping() {
		//Concession to Jackson
	}
	
	public RequestPattern getRequest() {
		return request;
	}
	
	public Response getResponse() {
		return response;
	}
	
	public void setRequest(RequestPattern request) {
		this.request = request;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((request == null) ? 0 : request.hashCode());
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
		if (request == null) {
			if (other.request != null)
				return false;
		} else if (!request.equals(other.request))
			return false;
		if (response == null) {
			if (other.response != null)
				return false;
		} else if (!response.equals(other.response))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RequestResponseMapping [request=" + request + ", response="
				+ response + "]";
	}
}
