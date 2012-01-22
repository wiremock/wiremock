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

public class ValuePatternTest {
	
	private ValuePattern valuePattern;
	
	@Before
	public void init() {
		valuePattern = new ValuePattern();
	}

	@Test
	public void matchesOnEqualTo() {
		valuePattern.setEqualTo("text/plain");
		assertTrue(valuePattern.isMatchFor("text/plain"));
	}
	
	@Test
	public void matchesOnRegex() {
		valuePattern.setMatches("[0-9]{6}");
		assertTrue(valuePattern.isMatchFor("938475"));
		assertFalse(valuePattern.isMatchFor("abcde"));
	}
	
	@Test
	public void matchesOnNegativeRegex() {
		valuePattern.setDoesNotMatch("[0-9]{6}");
		assertFalse(valuePattern.isMatchFor("938475"));
		assertTrue(valuePattern.isMatchFor("abcde"));
	}
	
	@Test
	public void matchesOnContains() {
		valuePattern.setContains("some text");
		assertFalse(valuePattern.isMatchFor("Nothing to see here"));
		assertTrue(valuePattern.isMatchFor("There's some text here"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatch() {
		valuePattern.setEqualTo("my-value");
		valuePattern.setMatches("[0-9]{6}");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherOrdering() {
		valuePattern.setMatches("[0-9]{6}");
		valuePattern.setEqualTo("my-value");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherDoesNotMatch() {
		valuePattern.setEqualTo("my-value");
		valuePattern.setDoesNotMatch("[0-9]{6}");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitZeroMatchTypes() {
		valuePattern.isMatchFor("blah");
	}
}
