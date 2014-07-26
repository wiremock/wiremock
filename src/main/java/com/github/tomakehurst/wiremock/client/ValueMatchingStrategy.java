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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.matching.ValuePattern;
import com.google.common.base.Function;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class ValueMatchingStrategy {

	private String equalTo;
	private String equalToJson;
	private String equalToXml;
    private String matchingXPath;
    private JSONCompareMode jsonCompareMode;
    private String matches;
    private String doesNotMatch;
    private String contains;
    private String matchesJsonPath;

    public ValuePattern asValuePattern() {
		ValuePattern pattern = new ValuePattern();
		pattern.setEqualTo(equalTo);
		pattern.setEqualToJson(equalToJson);
		pattern.setEqualToXml(equalToXml);
        pattern.setMatchesXPath(matchingXPath);
        pattern.setJsonCompareMode(jsonCompareMode);
		pattern.setMatches(matches);
		pattern.setDoesNotMatch(doesNotMatch);
		pattern.setContains(contains);
        pattern.setMatchesJsonPaths(matchesJsonPath);
		return pattern;
	}
	
	public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}

	public static Function<ValueMatchingStrategy, ValuePattern> toValuePattern = new Function<ValueMatchingStrategy, ValuePattern>() {
		public ValuePattern apply(ValueMatchingStrategy input) {
			return input.asValuePattern();
		}
	};
	
	public String getEqualToJson() {
        return equalToJson;
    }

    public void setEqualToJson(String equalToJson) {
        this.equalToJson = equalToJson;
    }

    public void setJsonCompareMode(JSONCompareMode jsonCompareMode) {
        this.jsonCompareMode = jsonCompareMode;
    }

    public String getEqualToXml() {
        return equalToXml;
    }

    public void setEqualToXml(String equalToXml) {
        this.equalToXml = equalToXml;
    }

    public String getMatchingXPath() {
        return matchingXPath;
    }

    public void setMatchingXPath(String matchingXPath) {
        this.matchingXPath = matchingXPath;
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

    public void setJsonMatchesPath(String jsonPaths) {
        this.matchesJsonPath = jsonPaths;
    }

    public JSONCompareMode getJsonCompareMode() {
        return jsonCompareMode;
    }
}
