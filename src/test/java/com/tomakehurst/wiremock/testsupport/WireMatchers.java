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
package com.tomakehurst.wiremock.testsupport;

import net.sf.json.test.JSONAssert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.matchers.TypeSafeMatcher;

public class WireMatchers {

	public static Matcher<String> jsonEqualTo(final String expectedJson) {
		return new TypeSafeMatcher<String>() {

			@Override
			public void describeTo(Description desc) {
			}

			@Override
			public boolean matchesSafely(String actualJson) {
				try {
					JSONAssert.assertJsonEquals(expectedJson, actualJson);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			
		};
	}

    public static Matcher<String> matches(final String regex) {
        return new TypeSafeMatcher<String>() {
    
            @Override
            public void describeTo(Description description) {
                description.appendText("Should match " + regex);
                
            }
    
            @Override
            public boolean matchesSafely(String actual) {
                return actual.matches(regex);
            }
            
        };
    }
}
