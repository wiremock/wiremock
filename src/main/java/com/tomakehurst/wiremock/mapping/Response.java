package com.tomakehurst.wiremock.mapping;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.http.ContentTypeHeader;
import com.tomakehurst.wiremock.http.HttpHeaders;

public class Response {

	private int status;
	private byte[] body = new byte[0];
	private HttpHeaders headers = new HttpHeaders();
	private boolean configured = true;
	
	public static Response notConfigured() {
		Response response = new Response(HttpURLConnection.HTTP_NOT_FOUND);
		response.setWasConfigured(false);
		return response;
	}
	
	public Response(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
	
	public void setBody(String body) {
		if (body == null) {
			return;
		}
		
		Optional<String> encoding = getEncodingFromHeaderIfAvailable();
		if (encoding.isPresent()) {
			this.body = body.getBytes(Charset.forName(encoding.get()));
		} else {
			this.body = body.getBytes(UTF_8);
		}
	}
	
	public void setBody(String body, String charset) {
		if (body == null) {
			return;
		}
		
		this.body = body.getBytes(Charset.forName(charset));
	}
	
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public String getBodyAsString() {
		Optional<String> encoding = getEncodingFromHeaderIfAvailable();
		if (encoding.isPresent()) {
			return new String(body, Charset.forName(encoding.get()));
		} else {
			return new String(body, UTF_8);
		}
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void addHeaders(Map<String, String> newHeaders) {
		if (newHeaders != null) {
			headers.putAll(newHeaders);
		}
	}
	
	public void applyTo(HttpServletResponse httpServletResponse) {
		httpServletResponse.setStatus(status);
		for (Map.Entry<String, String> header: headers.entrySet()) {
			httpServletResponse.addHeader(header.getKey(), header.getValue());
		}
		
		try {
			httpServletResponse.getOutputStream().write(body);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Optional<String> getEncodingFromHeaderIfAvailable() {
		if (!headers.containsKey(ContentTypeHeader.KEY)) {
			return Optional.absent();
		}
		
		ContentTypeHeader contentTypeHeader = new ContentTypeHeader(headers.get(ContentTypeHeader.KEY));
		return contentTypeHeader.encodingPart();
	}

	public boolean wasConfigured() {
		return configured;
	}

	public void setWasConfigured(boolean configured) {
		this.configured = configured;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", body=" + Arrays.toString(body)
				+ ", headers=" + headers + ", configured=" + configured + "]";
	}
	
	
}
