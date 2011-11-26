package com.tomakehurst.wiremock.mapping;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class RequestResponseMapping {

	private RequestPattern request;
	private Response response;
	private Priority priority;
	
	public RequestResponseMapping(RequestPattern requestPattern, Response response) {
		this.request = requestPattern;
		this.response = response;
	}
	
	public RequestResponseMapping() {
		//Concession to Jackson
	}
	
	public static RequestResponseMapping notConfigured() {
		return new RequestResponseMapping(new RequestPattern(), Response.notConfigured());
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

	public boolean priorityIs(Priority expectedPriority) {
		if (priority == null) {
			return expectedPriority == Priority.NORMAL;
		}
		
		return priority == expectedPriority;
	}
	
	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((request == null) ? 0 : request.hashCode());
		result = prime * result
				+ ((response == null) ? 0 : response.hashCode());
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
		RequestResponseMapping other = (RequestResponseMapping) obj;
		if (priority != other.priority) {
			return false;
		}
		if (request == null) {
			if (other.request != null) {
				return false;
			}
		} else if (!request.equals(other.request)) {
			return false;
		}
		if (response == null) {
			if (other.response != null) {
				return false;
			}
		} else if (!response.equals(other.response)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RequestResponseMapping [request=" + request + ", response="
				+ response + ", priority=" + priority + "]";
	}
	
	
}
