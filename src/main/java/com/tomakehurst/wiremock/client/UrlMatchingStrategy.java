package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.mapping.RequestPattern;

public class UrlMatchingStrategy {

	private String url;
	private String urlPattern;
	
	public void contributeTo(RequestPattern requestPattern) {
		requestPattern.setUrl(url);
		requestPattern.setUrlPattern(urlPattern);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}
	
	
}
