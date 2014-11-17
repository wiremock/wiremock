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

import java.nio.charset.Charset;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static java.net.HttpURLConnection.HTTP_OK;

public class ResponseDefinitionBuilder {

	private int status = HTTP_OK;
	private byte[] bodyContent;
    private boolean isBinaryBody = false;
    private String bodyFileName;
    private List<HttpHeader> headers = newArrayList();
    private List<HttpHeader> injectedHeaders = newArrayList();
	private Integer fixedDelayMilliseconds;
	private String proxyBaseUrl;
	private Fault fault;

    public static ResponseDefinition jsonResponse(Object body) {
        return new ResponseDefinitionBuilder()
                .withBody(Json.write(body))
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .build();
    }
	
	public ResponseDefinitionBuilder withStatus(int status) {
		this.status = status;
		return this;
	}
	
	public ResponseDefinitionBuilder withHeader(String key, String value) {
		headers.add(new HttpHeader(key, value));
		return this;
	}
	
	public ResponseDefinitionBuilder withInjectedHeader(String key, String value) {
		injectedHeaders.add(new HttpHeader(key, value));		
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
	
	public ResponseDefinitionBuilder proxiedFrom(String proxyBaseUrl) {
		this.proxyBaseUrl = proxyBaseUrl;
		return this;
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
		
        if (!injectedHeaders.isEmpty()) {
            response.setInjectedheaders(new HttpHeaders(injectedHeaders));
        }		
        
        response.setBodyFileName(bodyFileName);
		response.setFixedDelayMilliseconds(fixedDelayMilliseconds);
		response.setProxyBaseUrl(proxyBaseUrl);
		response.setFault(fault);
		return response;
	}
}
