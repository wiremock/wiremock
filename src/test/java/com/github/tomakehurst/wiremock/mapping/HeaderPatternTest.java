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

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class HeaderPatternTest {
	
	private HeaderPattern headerPattern;
	
	@Before
	public void init() {
		headerPattern = new HeaderPattern();
	}

	@Test
	public void matchesOnEqualTo() {
		headerPattern.setEqualTo("text/plain");
		assertTrue(headerPattern.isMatchFor("text/plain"));
	}
	
	@Test
	public void matchesOnRegex() {
		headerPattern.setMatches("[0-9]{6}");
		assertTrue(headerPattern.isMatchFor("938475"));
		assertFalse(headerPattern.isMatchFor("abcde"));
	}
	
	@Test
	public void matchesOnNegativeRegex() {
		headerPattern.setDoesNotMatch("[0-9]{6}");
		assertFalse(headerPattern.isMatchFor("938475"));
		assertTrue(headerPattern.isMatchFor("abcde"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatch() {
		headerPattern.setEqualTo("my-value");
		headerPattern.setMatches("[0-9]{6}");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherOrdering() {
		headerPattern.setMatches("[0-9]{6}");
		headerPattern.setEqualTo("my-value");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherDoesNotMatch() {
		headerPattern.setEqualTo("my-value");
		headerPattern.setDoesNotMatch("[0-9]{6}");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitZeroMatchTypes() {
		headerPattern.isMatchFor("blah");
	}
}
