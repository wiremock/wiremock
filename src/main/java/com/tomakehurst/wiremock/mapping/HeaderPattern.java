/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((doesNotMatch == null) ? 0 : doesNotMatch.hashCode());
		result = prime * result + ((equalTo == null) ? 0 : equalTo.hashCode());
		result = prime * result + ((matches == null) ? 0 : matches.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeaderPattern other = (HeaderPattern) obj;
		if (doesNotMatch == null) {
			if (other.doesNotMatch != null)
				return false;
		} else if (!doesNotMatch.equals(other.doesNotMatch))
			return false;
		if (equalTo == null) {
			if (other.equalTo != null)
				return false;
		} else if (!equalTo.equals(other.equalTo))
			return false;
		if (matches == null) {
			if (other.matches != null)
				return false;
		} else if (!matches.equals(other.matches))
			return false;
		return true;
	}
	
	
}
