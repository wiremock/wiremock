package com.tomakehurst.wiremock.client;

import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Map;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;

public class RequestPatternBuilder {

	private RequestMethod method;
	private UrlMatchingStrategy urlMatchingStrategy;
	private Map<String, HeaderMatchingStrategy> headers = newLinkedHashMap();
	private String bodyPattern;
	
	public RequestPatternBuilder(RequestMethod method,
			UrlMatchingStrategy urlMatchingStrategy) {
		this.method = method;
		this.urlMatchingStrategy = urlMatchingStrategy;
	}
	
	public RequestPatternBuilder withHeader(String key, HeaderMatchingStrategy headerMatchingStrategy) {
		headers.put(key, headerMatchingStrategy);
		return this;
	}
	
	public RequestPatternBuilder withBodyMatching(String bodyPattern) {
		this.bodyPattern = bodyPattern;
		return this;
	}

	public RequestPattern build() {
		RequestPattern requestPattern = new RequestPattern();
		requestPattern.setMethod(method);
		urlMatchingStrategy.contributeTo(requestPattern);
		for (Map.Entry<String, HeaderMatchingStrategy> header: headers.entrySet()) {
			header.getValue().contributeTo(requestPattern, header.getKey());
		}
		requestPattern.setBodyPattern(bodyPattern);
		
		return requestPattern;
	}
}
