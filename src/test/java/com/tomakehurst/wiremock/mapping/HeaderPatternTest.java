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
