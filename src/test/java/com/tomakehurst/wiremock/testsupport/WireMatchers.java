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

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.size;

import java.util.Iterator;

import net.sf.json.test.JSONAssert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.matchers.TypeSafeMatcher;

import com.google.common.base.Predicate;
import com.tomakehurst.wiremock.common.TextFile;

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
    
    public static <T> Matcher<Iterable<T>> hasExactly(final Matcher<T>... items) {
    	return new TypeSafeMatcher<Iterable<T>>() {

			@Override
			public void describeTo(Description desc) {
				desc.appendText("Collection must match exactly");
			}

			@Override
			public boolean matchesSafely(Iterable<T> actual) {
				Iterator<T> actualIter = actual.iterator();
				for (Matcher<T> matcher: items) {
					if (!matcher.matches(actualIter.next())) {
						return false;
					}
				}
				
				return !actualIter.hasNext();
			}
    		
    	};
    }
    
    public static <T> Matcher<Iterable<T>> hasExactlyIgnoringOrder(final Matcher<T>... items) {
    	return new TypeSafeMatcher<Iterable<T>>() {

			@Override
			public void describeTo(Description desc) {
				desc.appendText("Collection elements must match, but don't have to be in the same order.");
			}

			@Override
			public boolean matchesSafely(Iterable<T> actual) {
				if (size(actual) != items.length) {
					return false;
				}
				
				for (final Matcher<T> matcher: items) {
					if (find(actual, isMatchFor(matcher), null) == null) {
						return false;
					}
				}
				
				return true;
			}
    	};
    }
    
    private static <T> Predicate<T> isMatchFor(final Matcher<T> matcher) {
    	return new Predicate<T>() {
			public boolean apply(T input) {
				return matcher.matches(input);
			}
		};
    }
    
    public static Matcher<TextFile> fileNamed(final String name) {
        return new TypeSafeMatcher<TextFile>() {

            @Override
            public void describeTo(Description desc) {
            }

            @Override
            public boolean matchesSafely(TextFile textFile) {
                return textFile.name().equals(name);
            }
            
        };
    }
}
