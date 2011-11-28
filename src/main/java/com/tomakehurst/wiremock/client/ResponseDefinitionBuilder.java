package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;

public class ResponseDefinitionBuilder {

	private int status;
	private String bodyContent;
	private String bodyFileName;
	private HttpHeaders headers;
	private Integer fixedDelayMilliseconds;
	private String proxyBaseUrl;
	
	public ResponseDefinitionBuilder withStatus(int status) {
		this.status = status;
		return this;
	}
	
	public ResponseDefinitionBuilder withHeader(String key, String value) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		
		headers.put(key, value);
		return this;
	}
	
	public ResponseDefinitionBuilder withBodyFile(String fileName) {
		this.bodyFileName = fileName;
		return this;
	}
	
	public ResponseDefinitionBuilder withBody(String body) {
		this.bodyContent = body;
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
	
	public Response build() {
		Response response = new Response(status, bodyContent);
		response.setHeaders(headers);
		response.setBodyFileName(bodyFileName);
		response.setFixedDelayMilliseconds(fixedDelayMilliseconds);
		response.setProxyBaseUrl(proxyBaseUrl);
		return response;
	}
}
