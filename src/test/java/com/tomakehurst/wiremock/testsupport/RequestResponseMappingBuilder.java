package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.Response;

public class RequestResponseMappingBuilder {

	private String uriExpression = "/";
	private RequestMethod method = GET;
	private int responseStatus = 200;
	private String responseBody = "";
	
	public static RequestResponseMappingBuilder aMapping() {
		return new RequestResponseMappingBuilder();
	}

	public RequestResponseMappingBuilder withUriExpression(String uriExpression) {
		this.uriExpression = uriExpression;
		return this;
	}

	public RequestResponseMappingBuilder withMethod(RequestMethod method) {
		this.method = method;
		return this;
	}

	public RequestResponseMappingBuilder withResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
		return this;
	}

	public RequestResponseMappingBuilder withResponseBody(String responseBody) {
		this.responseBody = responseBody;
		return this;
	}
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = new RequestPattern(method, uriExpression);
		Response response = new Response(responseStatus, responseBody);
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		return mapping;
	}
}
