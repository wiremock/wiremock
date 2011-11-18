package com.tomakehurst.wiremock.mapping;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.tomakehurst.wiremock.http.HttpHeaders;

@JsonSerialize(include=Inclusion.NON_NULL)
public class Response {

	private int status;
	private String body;
	private String bodyFileName;
	private HttpHeaders headers;
	private boolean wasConfigured = true;
	
	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public Response(int statusCode, String bodyContent) {
		this.status = statusCode;
		this.body = bodyContent;
	}
	
	public Response() {
		this.status = HTTP_OK;
	}

	public static Response notFound() {
		return new Response(HTTP_NOT_FOUND, null);
	}
	
	public static Response ok() {
		return new Response(HTTP_OK, null);
	}
	
	public static Response created() {
		return new Response(HTTP_CREATED, null);
	}
	
	public static Response notConfigured() {
	    Response response = new Response(HTTP_NOT_FOUND, null);
	    response.wasConfigured = false;
	    return response;
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
		if (headers == null) {
			headers = new HttpHeaders();
		}
		
		headers.put(key, value);
	}

	public String getBodyFileName() {
		return bodyFileName;
	}

	public void setBodyFileName(String bodyFileName) {
		this.bodyFileName = bodyFileName;
	}
	
	public boolean wasConfigured() {
        return wasConfigured;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result
				+ ((bodyFileName == null) ? 0 : bodyFileName.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + status;
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
		Response other = (Response) obj;
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (bodyFileName == null) {
			if (other.bodyFileName != null) {
				return false;
			}
		} else if (!bodyFileName.equals(other.bodyFileName)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", body=" + body
				+ ", bodyFileName=" + bodyFileName + ", headers=" + headers
				+ "]";
	}

	
}
