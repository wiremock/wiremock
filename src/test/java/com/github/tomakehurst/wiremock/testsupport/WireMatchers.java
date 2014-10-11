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
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.google.common.base.Predicate;
import net.sf.json.test.JSONAssert;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.size;

public class WireMatchers {

	public static Matcher<String> equalToJson(final String expectedJson) {
		return new TypeSafeMatcher<String>() {

			@Override
			public void describeTo(Description desc) {
                desc.appendText("Expected:\n" + expectedJson);
			}

			@Override
			public boolean matchesSafely(String actualJson) {
				try {
					JSONAssert.assertJsonEquals(expectedJson, actualJson);
					return true;
				} catch (Throwable e) {
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
                desc.appendText("a text file named " + name);
            }

            @Override
            public boolean matchesSafely(TextFile textFile) {
                return textFile.name().endsWith(name);
            }
            
        };
    }

    public static Matcher<Date> isAfter(final String dateString) {
        return new TypeSafeMatcher<Date>() {
            @Override
            public boolean matchesSafely(Date date) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date compareDate = df.parse(dateString);
                    return date.after(compareDate);
                } catch (ParseException pe) {
                    throw new RuntimeException(pe);
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A date after " + dateString);
            }
        };
    }

    public static Matcher<Date> isToday() {
        return new TypeSafeMatcher<Date>() {
            @Override
            public boolean matchesSafely(Date date) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String today = df.format(new Date());
                return today.equals(df.format(date));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Today's date");
            }
        };
    }

    public static Matcher<HttpHeader> header(final String key, final String value) {
        return new TypeSafeMatcher<HttpHeader>() {
            @Override
            public boolean matchesSafely(HttpHeader httpHeader) {
                return httpHeader.key().equals(key) && httpHeader.containsValue(value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Header %s: %s", key, value));
            }
        };
    }
}
