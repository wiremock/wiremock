package com.tomakehurst.wiremock.testsupport;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
import com.tomakehurst.wiremock.mapping.RequestResponseMapping;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;

public class RequestResponseMappingBuilder {

	private String url = "/";
	private RequestMethod method = GET;
	private int responseStatus = 200;
	private String responseBody = "";
	private HttpHeaders headers = new HttpHeaders();
	
	public static RequestResponseMappingBuilder aMapping() {
		return new RequestResponseMappingBuilder();
	}

	public RequestResponseMappingBuilder withUrl(String url) {
		this.url = url;
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
	
	public RequestResponseMappingBuilder withHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}
	
	public RequestResponseMapping build() {
		RequestPattern requestPattern = new RequestPattern(method, url);
		ResponseDefinition response = new ResponseDefinition(responseStatus, responseBody);
		response.setHeaders(headers);
		RequestResponseMapping mapping = new RequestResponseMapping(requestPattern, response);
		return mapping;
	}
}
