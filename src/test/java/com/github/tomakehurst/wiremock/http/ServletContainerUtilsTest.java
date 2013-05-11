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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.jetty.ServletContainerUtils;
import com.github.tomakehurst.wiremock.testsupport.MockHttpServletRequest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
