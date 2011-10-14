package com.tomakehurst.wiremock.mapping;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class Response {

	private int status;
	private String body;
	
	public Response(int statusCode, String bodyContent) {
		this.status = statusCode;
		this.body = bodyContent;
	}
	
	public Response() {
	}

	public static Response notFound() {
		return new Response(HTTP_NOT_FOUND, "");
	}
	
	public static Response ok() {
		return new Response(HTTP_OK, "");
	}
	
	public static Response created() {
		return new Response(HTTP_CREATED, "");
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void applyTo(HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(status);
		try {
			httpServletResponse.getWriter().write(body);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((body == null) ? 0 : body.hashCode());
		result = prime * result + status;
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
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (status != other.status)
			return false;
		return true;
	}
	
	
}
