/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomakehurst.wiremock.mapping;

import static com.google.common.base.Charsets.UTF_8;
import static com.tomakehurst.wiremock.http.HttpServletResponseUtils.getUnderlyingSocketFrom;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.tomakehurst.wiremock.http.ContentTypeHeader;
import com.tomakehurst.wiremock.http.Fault;
import com.tomakehurst.wiremock.http.HttpHeaders;

public class Response {

	private final int status;
	private byte[] body = new byte[0];
	private final HttpHeaders headers = new HttpHeaders();
	private boolean configured = true;
	private Fault fault;
	
	public static Response notConfigured() {
		final Response response = new Response(HTTP_NOT_FOUND);
		response.setWasConfigured(false);
		return response;
	}
	
	public static Response error(final Throwable ex) {
	    final Response response = new Response(HTTP_INTERNAL_ERROR);
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ex.printStackTrace(new PrintStream(out));
	    response.body = out.toByteArray();
        response.setWasConfigured(false);
        return response;
	}
	
	public Response(final int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
	
	public void setBody(final String body) {
		if (body == null) {
			return;
		}
		
		final Optional<String> encoding = getEncodingFromHeaderIfAvailable();
		if (encoding.isPresent()) {
			this.body = body.getBytes(Charset.forName(encoding.get()));
		} else {
			this.body = body.getBytes(UTF_8);
		}
	}
	
	public void setBody(final String body, final String charset) {
		if (body == null) {
			return;
		}
		
		this.body = body.getBytes(Charset.forName(charset));
	}
	
	public void setBody(final byte[] body) {
		this.body = body;
	}
	
	public String getBodyAsString() {
		final Optional<String> encoding = getEncodingFromHeaderIfAvailable();
		if (encoding.isPresent()) {
			return new String(body, Charset.forName(encoding.get()));
		} else {
			return new String(body, UTF_8);
		}
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	public void addHeader(final String key, final String value) {
		headers.put(key, value);
	}
	
	public void addHeaders(final Map<String, String> newHeaders) {
		if (newHeaders != null) {
			headers.putAll(newHeaders);
		}
	}
	
	public void applyTo(final HttpServletResponse httpServletResponse) {
		if (fault != null) {
			fault.apply(httpServletResponse, getUnderlyingSocketFrom(httpServletResponse));
			return;
		}
		
		httpServletResponse.setStatus(status);
		for (final Map.Entry<String, String> header: headers.entrySet()) {
			httpServletResponse.addHeader(header.getKey(), header.getValue());
		}
		
		writeAndTranslateExceptions(httpServletResponse, body);
	}
	
	private static void writeAndTranslateExceptions(final HttpServletResponse httpServletResponse, final byte[] content) {
		try {	
			httpServletResponse.getOutputStream().write(content);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Optional<String> getEncodingFromHeaderIfAvailable() {
		if (!headers.containsKey(ContentTypeHeader.KEY)) {
			return Optional.absent();
		}
		
		final ContentTypeHeader contentTypeHeader = new ContentTypeHeader(headers.get(ContentTypeHeader.KEY));
		return contentTypeHeader.encodingPart();
	}

	public boolean wasConfigured() {
		return configured;
	}

	public void setWasConfigured(final boolean configured) {
		this.configured = configured;
	}

	public void setFault(final Fault fault) {
		this.fault = fault;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", body=" + Arrays.toString(body)
				+ ", headers=" + headers + ", configured=" + configured
				+ ", fault=" + fault + "]";
	}

	
	
}
