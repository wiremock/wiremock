package com.tomakehurst.wiremock.client;

import com.tomakehurst.wiremock.mapping.HeaderPattern;
import com.tomakehurst.wiremock.mapping.RequestPattern;

public class HeaderMatchingStrategy {

	private String equalTo;
	private String matches;
	private String doesNotMatch;
	
	public void contributeTo(RequestPattern requestPattern, String key) {
		HeaderPattern pattern = new HeaderPattern();
		pattern.setEqualTo(equalTo);
		pattern.setMatches(matches);
		pattern.setDoesNotMatch(doesNotMatch);
		requestPattern.addHeader(key, pattern);
	}
	
	public String getEqualTo() {
		return equalTo;
	}
	
	public void setEqualTo(String equalTo) {
		this.equalTo = equalTo;
	}
	
	public String getMatches() {
		return matches;
	}
	
	public void setMatches(String matches) {
		this.matches = matches;
	}
	
	public String getDoesNotMatch() {
		return doesNotMatch;
	}
	
	public void setDoesNotMatch(String doesNotMatch) {
		this.doesNotMatch = doesNotMatch;
	}

	
}
