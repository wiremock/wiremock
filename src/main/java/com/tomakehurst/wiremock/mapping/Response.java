package com.tomakehurst.wiremock.mapping;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.http.HttpHeaders;

public class Response {

	private int status;
	private String body;
	private HttpHeaders headers;
	
	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public Response(int statusCode, String bodyContent) {
		this.status = statusCode;
		this.body = bodyContent;
		this.headers = new HttpHeaders();
	}
	
	public Response() {
		this.body = "";
		this.headers = new HttpHeaders();
		this.status = HTTP_OK;
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
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public void applyTo(HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(status);
		try {
			for (Map.Entry<String, String> header: headers.entrySet()) {
				httpServletResponse.addHeader(header.getKey(), header.getValue());
			}
			
			httpServletResponse.getWriter().write(body);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
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
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", body=" + body + ", headers="
				+ headers + "]";
	}
	
	
}
