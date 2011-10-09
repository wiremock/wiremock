package com.tomakehurst.wiremock;

public class Response {

	private int statusCode;
	private String bodyContent;
	
	public Response(int statusCode, String bodyContent) {
		this.statusCode = statusCode;
		this.bodyContent = bodyContent;
	}
	
	public static Response notFound() {
		return new Response(404, "");
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public String getBodyContent() {
		return bodyContent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bodyContent == null) ? 0 : bodyContent.hashCode());
		result = prime * result + statusCode;
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
		Response other = (Response) obj;
		if (bodyContent == null) {
			if (other.bodyContent != null)
				return false;
		} else if (!bodyContent.equals(other.bodyContent))
			return false;
		if (statusCode != other.statusCode)
			return false;
		return true;
	}
	
	
}
