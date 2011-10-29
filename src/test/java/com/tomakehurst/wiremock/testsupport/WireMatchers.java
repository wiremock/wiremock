package com.tomakehurst.wiremock.testsupport;

import net.sf.json.test.JSONAssert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

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
}
