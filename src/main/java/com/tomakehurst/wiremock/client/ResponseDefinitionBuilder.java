package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.mapping.Response;

public class ResponseDefinitionBuilder {

	private int status;
	private String bodyContent = "";
	
	public ResponseDefinitionBuilder withStatus(int status) {
		this.status = status;
		return this;
	}
	
	public Response build() {
		return new Response(status, bodyContent);
	}
}
