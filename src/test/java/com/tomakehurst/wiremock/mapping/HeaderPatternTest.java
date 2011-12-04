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
		headerPattern.setEqualTo("/my/url");
		assertTrue(headerPattern.isMatchFor("/my/url"));
	}
	
	@Test
	public void matchesOnRegex() {
		headerPattern.setMatches("/match/[0-9]{6}/this");
		assertTrue(headerPattern.isMatchFor("/match/938475/this"));
		assertFalse(headerPattern.isMatchFor("/match/abcde/this"));
	}
	
	@Test
	public void matchesOnNegativeRegex() {
		headerPattern.setDoesNotMatch("/match/[0-9]{6}/this");
		assertFalse(headerPattern.isMatchFor("/match/938475/this"));
		assertTrue(headerPattern.isMatchFor("/match/abcde/this"));
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatch() {
		headerPattern.setEqualTo("/my/url");
		headerPattern.setMatches("/match/[0-9]{6}/this");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherOrdering() {
		headerPattern.setMatches("/match/[0-9]{6}/this");
		headerPattern.setEqualTo("/my/url");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitMoreThanOneTypeOfMatchWithOtherDoesNotMatch() {
		headerPattern.setEqualTo("/my/url");
		headerPattern.setDoesNotMatch("/match/[0-9]{6}/this");
	}
	
	@Test(expected=IllegalStateException.class)
	public void doesNotPermitZeroMatchTypes() {
		headerPattern.isMatchFor("blah");
	}
}
