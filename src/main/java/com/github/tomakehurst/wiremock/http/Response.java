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
package com.github.tomakehurst.wiremock.http;

import java.nio.charset.Charset;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.http.HttpHeaders.noHeaders;
import static com.google.common.base.Charsets.UTF_8;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class Response {

	private final int status;
	private final byte[] body;
	private final HttpHeaders headers;
	private final boolean configured;
	private final Fault fault;
	private final boolean fromProxy;
	
	public static Response notConfigured() {
        Response response = new Response(HTTP_NOT_FOUND,
                (byte[]) null,
                noHeaders(),
                false,
                null,
                false);
		return response;
	}

    public static Builder response() {
        return new Builder();
    }

	public Response(int status, byte[] body, HttpHeaders headers, boolean configured, Fault fault, boolean fromProxy) {
		this.status = status;
        this.body = body;
        this.headers = headers;
        this.configured = configured;
        this.fault = fault;
        this.fromProxy = fromProxy;
	}

    public Response(int status, String body, HttpHeaders headers, boolean configured, Fault fault, boolean fromProxy) {
        this.status = status;
        this.headers = headers;
        this.body = body == null ? null : body.getBytes(encodingFromContentTypeHeaderOrUtf8());
        this.configured = configured;
        this.fault = fault;
        this.fromProxy = fromProxy;
    }

	public int getStatus() {
		return status;
	}

    public byte[] getBody() {
        return body;
    }
	
	public String getBodyAsString() {
        return new String(body, encodingFromContentTypeHeaderOrUtf8());
	}
	
	public HttpHeaders getHeaders() {
		return headers;
	}

    public Fault getFault() {
        return fault;
    }

    private Charset encodingFromContentTypeHeaderOrUtf8() {
        ContentTypeHeader contentTypeHeader = headers.getContentTypeHeader();
        if (contentTypeHeader.isPresent() && contentTypeHeader.encodingPart().isPresent()) {
            return Charset.forName(contentTypeHeader.encodingPart().get());
        }

        return UTF_8;
    }
	
	public boolean wasConfigured() {
		return configured;
	}

    public boolean isFromProxy() {
        return fromProxy;
    }

    @Override
    public String toString() {
        return "Response [status=" + status + ", body=" + Arrays.toString(body) + ", headers=" + headers
                + ", configured=" + configured + ", fault=" + fault + ", fromProxy=" + fromProxy + "]";
    }

    public static class Builder {
        private int status = HTTP_OK;
        private byte[] body;
        private String bodyString;
        private HttpHeaders headers = new HttpHeaders();
        private boolean configured = true;
        private Fault fault;
        private boolean fromProxy;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            ensureOnlyOneBodySet();
            return this;
        }

        public Builder body(String body) {
            this.bodyString = body;
            ensureOnlyOneBodySet();
            return this;
        }

        private void ensureOnlyOneBodySet() {
            if (body != null && bodyString != null) {
                throw new IllegalStateException("Body should either be set as a String or byte[], not both");
            }
        }

        public Builder headers(HttpHeaders headers) {
            this.headers = headers == null ? noHeaders() : headers;
            return this;
        }

        public Builder configured(boolean configured) {
            this.configured = configured;
            return this;
        }

        public Builder fault(Fault fault) {
            this.fault = fault;
            return this;
        }

        public Builder fromProxy(boolean fromProxy) {
            this.fromProxy = fromProxy;
            return this;
        }

        public Response build() {
            if (body != null) {
                return new Response(status, body, headers, configured, fault, fromProxy);
            } else if (bodyString != null) {
                return new Response(status, bodyString, headers, configured, fault, fromProxy);
            } else {
                return new Response(status, new byte[0], headers, configured, fault, fromProxy);
            }
        }
    }

}
