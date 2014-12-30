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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.collect.Lists;

import java.nio.charset.Charset;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;

public class ResponseDefinitionBuilder {

	protected int status = HTTP_OK;
	protected byte[] bodyContent;
	protected boolean isBinaryBody = false;
	protected String bodyFileName;
	protected List<HttpHeader> headers = newArrayList();
	protected Integer fixedDelayMilliseconds;
	protected String proxyBaseUrl;
	protected Fault fault;
	protected List<String> responseTransformerNames;

	public static ResponseDefinitionBuilder like(ResponseDefinition responseDefinition) {
		ResponseDefinitionBuilder builder = new ResponseDefinitionBuilder();
		builder.status = responseDefinition.getStatus();
		builder.headers = responseDefinition.getHeaders() != null ?
				newArrayList(responseDefinition.getHeaders().all()) :
				Lists.<HttpHeader>newArrayList();
		builder.bodyContent = responseDefinition.getByteBody();
		builder.isBinaryBody = responseDefinition.specifiesBinaryBodyContent();
		builder.bodyFileName = responseDefinition.getBodyFileName();
		builder.fixedDelayMilliseconds = responseDefinition.getFixedDelayMilliseconds();
		builder.proxyBaseUrl = responseDefinition.getProxyBaseUrl();
		builder.fault = responseDefinition.getFault();
		builder.responseTransformerNames = responseDefinition.getResponseTransformers();
		return builder;
	}

    public static ResponseDefinition jsonResponse(Object body) {
        return new ResponseDefinitionBuilder()
                .withBody(Json.write(body))
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .build();
    }

	public ResponseDefinitionBuilder but() {
		return this;
	}
	
	public ResponseDefinitionBuilder withStatus(int status) {
		this.status = status;
		return this;
	}
	
	public ResponseDefinitionBuilder withHeader(String key, String value) {
		headers.add(new HttpHeader(key, value));
		return this;
	}
	
	public ResponseDefinitionBuilder withBodyFile(String fileName) {
		this.bodyFileName = fileName;
		return this;
	}
	
	public ResponseDefinitionBuilder withBody(String body) {
		this.bodyContent = body.getBytes(Charset.forName(UTF_8.name()));
        isBinaryBody = false;
		return this;
	}

    public ResponseDefinitionBuilder withBody(byte[] body) {
        this.bodyContent = body;
        isBinaryBody = true;
        return this;
    }

    public ResponseDefinitionBuilder withFixedDelay(Integer milliseconds) {
        this.fixedDelayMilliseconds = milliseconds;
        return this;
    }

	public ResponseDefinitionBuilder withTransform(String... responseTransformerNames) {
		this.responseTransformerNames = asList(responseTransformerNames);
		return this;
	}

	public ProxyResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
		this.proxyBaseUrl = proxyBaseUrl;
		return new ProxyResponseDefinitionBuilder(this);
	}

	public static class ProxyResponseDefinitionBuilder extends ResponseDefinitionBuilder {

		private List<HttpHeader> additionalRequestHeaders = newArrayList();

		public ProxyResponseDefinitionBuilder(ResponseDefinitionBuilder from) {
			this.status = from.status;
			this.headers = from.headers;
			this.bodyContent = from.bodyContent;
			this.bodyFileName = from.bodyFileName;
			this.fault = from.fault;
			this.fixedDelayMilliseconds = from.fixedDelayMilliseconds;
			this.isBinaryBody = from.isBinaryBody;
			this.proxyBaseUrl = from.proxyBaseUrl;
			this.responseTransformerNames = from.responseTransformerNames;
		}

		public ProxyResponseDefinitionBuilder withAdditionalRequestHeader(String key, String value) {
			additionalRequestHeaders.add(new HttpHeader(key, value));
			return this;
		}

		@Override
		public ResponseDefinition build() {
			ResponseDefinition response = super.build();

			if (!additionalRequestHeaders.isEmpty()) {
				response.setAdditionalProxyRequestHeaders(new HttpHeaders(additionalRequestHeaders));
			}

			return response;
		}
	}
	
	public ResponseDefinitionBuilder withFault(Fault fault) {
		this.fault = fault;
		return this;
	}
	
	public ResponseDefinition build() {
        ResponseDefinition response;

        if(isBinaryBody) {
	        response = new ResponseDefinition(status, bodyContent);
        } else {
            if(bodyContent==null) {
                response = new ResponseDefinition(status, (String)null);
            } else {
                response = new ResponseDefinition(status, new String(bodyContent,Charset.forName(UTF_8.name())));
            }
        }

        if (!headers.isEmpty()) {
            response.setHeaders(new HttpHeaders(headers));
        }
		
        response.setBodyFileName(bodyFileName);
		response.setFixedDelayMilliseconds(fixedDelayMilliseconds);
		response.setProxyBaseUrl(proxyBaseUrl);
		response.setFault(fault);
		response.setResponseTransformers(responseTransformerNames);
		return response;
	}
}
