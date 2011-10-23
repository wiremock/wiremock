package com.tomakehurst.wiremock.mapping;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
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
		checkOneMatchTypeSpecified();
		if (equalTo != null) {
			return headerValue.equals(equalTo);
		} else if (matches != null) {
			return headerValue.matches(matches);
		} else if (doesNotMatch != null) {
			return !headerValue.matches(doesNotMatch);
		}
		
		return false;
	}
	
	private void checkNoMoreThanOneMatchTypeSpecified() {
		if (count(equalTo, matches, doesNotMatch) > 1) {
			throw new IllegalStateException("Only one type of match may be specified");
		}
	}
	
	private void checkOneMatchTypeSpecified() {
		if (count(equalTo, matches, doesNotMatch) == 0) {
			throw new IllegalStateException("One match type must be specified");
		}
	}
	
	private int count(Object... objects) {
		int counter = 0;
		for (Object obj: objects) {
			if (obj != null) {
				counter++;
			}
		}
		
		return counter;
	}
	
	public void setEqualTo(String equalTo) {
		this.equalTo = equalTo;
		checkNoMoreThanOneMatchTypeSpecified();
	}
	
	public void setMatches(String matches) {
		this.matches = matches;
		checkNoMoreThanOneMatchTypeSpecified();
	}

	public void setDoesNotMatch(String doesNotMatch) {
		this.doesNotMatch = doesNotMatch;
		checkNoMoreThanOneMatchTypeSpecified();
	}

	public String getEqualTo() {
		return equalTo;
	}

	public String getMatches() {
		return matches;
	}

	public String getDoesNotMatch() {
		return doesNotMatch;
	}
	
}
