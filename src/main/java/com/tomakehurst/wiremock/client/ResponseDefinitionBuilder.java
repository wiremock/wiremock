package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.mapping.Response;

public class ResponseDefinitionBuilder {

	private int status;
	private String bodyContent;
	private HttpHeaders headers;
	
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
	
	public ResponseDefinitionBuilder withBody(String body) {
		this.bodyContent = body;
		return this;
	}
	
	public Response build() {
		Response response = new Response(status, bodyContent);
		response.setHeaders(headers);
		return response;
	}
}
