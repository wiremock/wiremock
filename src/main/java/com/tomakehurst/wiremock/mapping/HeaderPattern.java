package com.tomakehurst.wiremock.mapping;

public class HeaderPattern {

	private String equalTo;
	private String matches;
	private String doesNotMatch;
	
	public static HeaderPattern equalTo(String value) {
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setEqualTo(value);
		return headerPattern;
	}
	
	public static HeaderPattern matches(String value) {
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setMatches(value);
		return headerPattern;
	}
	
	public boolean isMatchFor(String headerValue) {
		validate();
		if (equalTo != null) {
			return headerValue.equals(equalTo);
		} else if (matches != null) {
			return headerValue.matches(matches);
		} else if (doesNotMatch != null) {
			return !headerValue.matches(doesNotMatch);
		}
		
		return false;
	}
	
	private void validate() {
		int matchTypeCount = 0;
		if (equalTo != null) matchTypeCount++;
		if (matches != null) matchTypeCount++;
		if (doesNotMatch != null) matchTypeCount++;
		
		if (matchTypeCount == 0) {
			throw new IllegalStateException("One match type must be specified");
		}
		
		if (matchTypeCount > 1) {
			throw new IllegalStateException("Only one type of match may be specified");
		}
	}
	
	public void setEqualTo(String equalTo) {
		this.equalTo = equalTo;
		validate();
	}
	
	public void setMatches(String matches) {
		this.matches = matches;
		validate();
	}

	public void setDoesNotMatch(String doesNotMatch) {
		this.doesNotMatch = doesNotMatch;
		validate();
	}
	
}
