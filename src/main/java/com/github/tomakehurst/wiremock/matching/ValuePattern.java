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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.xml.sax.SAXException;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static java.util.regex.Pattern.DOTALL;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@JsonSerialize(include=Inclusion.NON_NULL)
public class ValuePattern {

    static {
        XMLUnit.setIgnoreWhitespace(true);
    }

    private String equalToJson;
    private String equalToXml;
    private JSONCompareMode jsonCompareMode;
	private String equalTo;
	private String contains;
	private String matches;
	private String doesNotMatch;
    private Boolean absent;
    private String matchesJsonPath;

	public static ValuePattern equalTo(String value) {
		ValuePattern valuePattern = new ValuePattern();
		valuePattern.setEqualTo(value);
		return valuePattern;
	}
	
    public static ValuePattern equalToJson(String value) {
        ValuePattern valuePattern = new ValuePattern();
        valuePattern.setEqualToJson(value);
        return valuePattern;
    }

    public static ValuePattern equalToXml(String value) {
        ValuePattern valuePattern = new ValuePattern();
        valuePattern.setEqualToXml(value);
        return valuePattern;
    }

    public static ValuePattern equalToJson(String value, JSONCompareMode jsonCompareMode) {
        ValuePattern valuePattern = new ValuePattern();
        valuePattern.setEqualToJson(value);
        valuePattern.setJsonCompareMode(jsonCompareMode);
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
        } else if (equalToJson != null) {
            return isEqualJson(value);
        } else if (equalToXml != null) {
            return isEqualXml(value);
        } else if (equalTo != null) {
			return value.equals(equalTo);
		} else if (contains != null) {
			return value.contains(contains);
		} else if (matches != null) {
			return isMatch(matches, value);
		} else if (doesNotMatch != null) {
			return !isMatch(doesNotMatch, value);
		} else if (matchesJsonPath != null) {
            return isJsonPathMatch(value);
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
	
    private boolean isEqualJson(String value) {
        JSONCompareResult result;
        try {
            result = compareJSON(equalToJson, value, Optional.fromNullable(jsonCompareMode).or(NON_EXTENSIBLE));
        } catch (JSONException e) {
            return false;
        }
        return result.passed();
    }
	
    private boolean isEqualXml(String value) {
        try {
            Diff diff = XMLUnit.compareXML(equalToXml, value);
            return diff.similar();
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
	
	private boolean isMatch(String regex, String value) {
		Pattern pattern = Pattern.compile(regex, DOTALL);
		return pattern.matcher(value).matches();
	}

    private boolean isJsonPathMatch(String value) {
        try {
            Object obj = JsonPath.read(value, matchesJsonPath);
            if (obj instanceof JSONArray) {
                return ((JSONArray) obj).size() > 0;
            }

            if (obj instanceof JSONObject) {
                return ((JSONObject) obj).size() > 0;
            }

            return obj != null;
        } catch (Exception e) {
            String error;
            if (e.getMessage().equalsIgnoreCase("invalid path")) {
                error = "the JSON path didn't match the document structure";
            }
            else if (e.getMessage().equalsIgnoreCase("invalid container object")) {
                error = "the JSON document couldn't be parsed";
            } else {
                error = "of error '" + e.getMessage() + "'";
            }

            String message = String.format(
                    "Warning: JSON path expression '%s' failed to match document '%s' because %s",
                    matchesJsonPath, value, error);
            notifier().info(message);
            return false;
        }
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
		return count(equalToJson, equalToXml, equalTo, contains, matches, doesNotMatch, absent, matchesJsonPath);
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
	
    public void setEqualToJson(String equalToJson) {
        this.equalToJson = equalToJson;
        checkNoMoreThanOneMatchTypeSpecified();
    }
    
    public void setEqualToXml(String equalToXml) {
        this.equalToXml = equalToXml;
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

    public void setMatchesJsonPaths(String matchesJsonPath) {
        this.matchesJsonPath = matchesJsonPath;
        checkNoMoreThanOneMatchTypeSpecified();
    }

	public String getEqualTo() {
		return equalTo;
	}
	
    public String getEqualToJson() {
        return equalToJson;
    }

    public String getEqualToXml() {
        return equalToXml;
    }

    public JSONCompareMode getJsonCompareMode() {
        return jsonCompareMode;
    }

    public void setJsonCompareMode(JSONCompareMode jsonCompareMode) {
        this.jsonCompareMode = jsonCompareMode;
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

    public String getMatchesJsonPath() {
        return matchesJsonPath;
    }

    public boolean nullSafeIsAbsent() {
        return (absent != null && absent);
    }
	
	@Override
	public String toString() {
	    if (equalToJson != null) {
            return "equalJson " + equalToJson;
        } else if (equalToXml != null) {
            return "equalXml " + equalToXml;
	    } else if (equalTo != null) {
			return "equal " + equalTo;
		} else if (contains != null) {
			return "contains " + contains;
		} else if (matches != null) {
			return "matches " + matches;
		} else if (doesNotMatch != null) {
			return "not match " + doesNotMatch;
        } else if (matchesJsonPath != null) {
            return "matches JSON path " + matchesJsonPath;
		} else {
            return "is absent";
        }
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValuePattern that = (ValuePattern) o;

        if (absent != null ? !absent.equals(that.absent) : that.absent != null) return false;
        if (contains != null ? !contains.equals(that.contains) : that.contains != null) return false;
        if (doesNotMatch != null ? !doesNotMatch.equals(that.doesNotMatch) : that.doesNotMatch != null) return false;
        if (equalTo != null ? !equalTo.equals(that.equalTo) : that.equalTo != null) return false;
        if (equalToJson != null ? !equalToJson.equals(that.equalToJson) : that.equalToJson != null) return false;
        if (equalToXml != null ? !equalToXml.equals(that.equalToXml) : that.equalToXml != null) return false;
        if (matches != null ? !matches.equals(that.matches) : that.matches != null) return false;
        if (matchesJsonPath != null ? !matchesJsonPath.equals(that.matchesJsonPath) : that.matchesJsonPath != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = equalTo != null ? equalTo.hashCode() : 0;
        result = 31 * result + (equalToJson != null ? equalToJson.hashCode() : 0);
        result = 31 * result + (equalToXml != null ? equalToXml.hashCode() : 0);
        result = 31 * result + (contains != null ? contains.hashCode() : 0);
        result = 31 * result + (matches != null ? matches.hashCode() : 0);
        result = 31 * result + (doesNotMatch != null ? doesNotMatch.hashCode() : 0);
        result = 31 * result + (absent != null ? absent.hashCode() : 0);
        result = 31 * result + (matchesJsonPath != null ? matchesJsonPath.hashCode() : 0);
        return result;
    }
}
