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
package com.github.tomakehurst.wiremock.mapping;

import static java.util.regex.Pattern.DOTALL;

import java.util.regex.Pattern;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.common.base.Predicate;

@JsonSerialize(include=Inclusion.NON_NULL)
public class ValuePattern {

	private String equalTo;
	private String contains;
	private String matches;
	private String doesNotMatch;
    private Boolean absent;
	
	public static ValuePattern equalTo(String value) {
		ValuePattern valuePattern = new ValuePattern();
		valuePattern.setEqualTo(value);
		return valuePattern;
	}
	
	public static ValuePattern containing(String value) {
		ValuePattern valuePattern = new ValuePattern();
		valuePattern.setContains(value);
		return valuePattern;
	}
	
	public static ValuePattern matches(String value) {
		ValuePattern valuePattern = new ValuePattern();
		valuePattern.setMatches(value);
		return valuePattern;
	}

    public static ValuePattern absent() {
        ValuePattern valuePattern = new ValuePattern();
        valuePattern.absent = true;
        return valuePattern;
    }
	
	public boolean isMatchFor(String value) {
		checkOneMatchTypeSpecified();

        if (absent != null) {
            return (absent && value == null);
        } else if (equalTo != null) {
			return value.equals(equalTo);
		} else if (contains != null) {
			return value.contains(contains);
		} else if (matches != null) {
			return isMatch(matches, value);
		} else if (doesNotMatch != null) {
			return !isMatch(doesNotMatch, value);
		}
		
		return false;
	}
	
	public static Predicate<ValuePattern> matching(final String value) {
		return new Predicate<ValuePattern>() {
			public boolean apply(ValuePattern input) {
				return input.isMatchFor(value);
			}
		};
	}
	
	private boolean isMatch(String regex, String value) {
		Pattern pattern = Pattern.compile(regex, DOTALL);
		return pattern.matcher(value).matches();
	}
	
	private void checkNoMoreThanOneMatchTypeSpecified() {
		if (countAllAttributes() > 1) {
			throw new IllegalStateException("Only one type of match may be specified");
		}
	}

	private void checkOneMatchTypeSpecified() {
		if (countAllAttributes() == 0) {
			throw new IllegalStateException("One match type must be specified");
		}
	}
	
	private int countAllAttributes() {
		return count(equalTo, contains, matches, doesNotMatch, absent);
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
	
	public void setContains(String contains) {
		this.contains = contains;
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

    public void setAbsent(Boolean absent) {
        this.absent = absent;
        checkNoMoreThanOneMatchTypeSpecified();
    }

	public String getEqualTo() {
		return equalTo;
	}
	
	public String getContains() {
		return contains;
	}

	public String getMatches() {
		return matches;
	}

	public String getDoesNotMatch() {
		return doesNotMatch;
	}

    public Boolean isAbsent() {
        return absent;
    }

    public boolean nullSafeIsAbsent() {
        return (absent != null && absent);
    }
	
	@Override
	public String toString() {
		if (equalTo != null) {
			return "equal " + equalTo;
		} else if (contains != null) {
			return "contains " + contains;
		} else if (matches != null) {
			return "matches " + matches;
		} else {
			return "not match " + doesNotMatch; 
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contains == null) ? 0 : contains.hashCode());
		result = prime * result
				+ ((doesNotMatch == null) ? 0 : doesNotMatch.hashCode());
		result = prime * result + ((equalTo == null) ? 0 : equalTo.hashCode());
		result = prime * result + ((matches == null) ? 0 : matches.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ValuePattern other = (ValuePattern) obj;
		if (contains == null) {
			if (other.contains != null) {
				return false;
			}
		} else if (!contains.equals(other.contains)) {
			return false;
		}
		if (doesNotMatch == null) {
			if (other.doesNotMatch != null) {
				return false;
			}
		} else if (!doesNotMatch.equals(other.doesNotMatch)) {
			return false;
		}
		if (equalTo == null) {
			if (other.equalTo != null) {
				return false;
			}
		} else if (!equalTo.equals(other.equalTo)) {
			return false;
		}
		if (matches == null) {
			if (other.matches != null) {
				return false;
			}
		} else if (!matches.equals(other.matches)) {
			return false;
		}
		return true;
	}
}
