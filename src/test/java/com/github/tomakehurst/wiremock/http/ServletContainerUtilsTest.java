package com.github.tomakehurst.wiremock.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.tomakehurst.wiremock.testsupport.MockHttpServletRequest;

public class ServletContainerUtilsTest {

	@Test
	public void isBrowserProxyRequestReturnsFalseWhenUnderlyingUriNotPresentInRequest() {
		assertFalse(ServletContainerUtils.isBrowserProxyRequest(new RequestWithoutUriField()));
	}
	
	@Test
	public void isBrowserProxyRequestReturnsFalseWhenUnderlyingUriPresentInRequest() {
		assertTrue(ServletContainerUtils.isBrowserProxyRequest(new RequestWithUriField()));
	}
	
	private static class RequestWithoutUriField extends MockHttpServletRequest {
	}
	
	private static class RequestWithUriField extends MockHttpServletRequest {
		@SuppressWarnings("unused")
		private String _uri = "http://somehost.com:8090/things";
	}
}
