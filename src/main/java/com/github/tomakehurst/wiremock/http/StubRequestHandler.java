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

import com.github.tomakehurst.wiremock.stubbing.StubMappings;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class StubRequestHandler extends AbstractRequestHandler {
	
	private final StubMappings stubMappings;
	private final boolean browserProxyingEnabled;

	public StubRequestHandler(StubMappings stubMappings, ResponseRenderer responseRenderer, boolean browserProxyingEnabled) {
		super(responseRenderer);
		this.stubMappings = stubMappings;
		this.browserProxyingEnabled = browserProxyingEnabled;
	}
	
	@Override
	public ResponseDefinition handleRequest(Request request) {
        notifier().info("Received request to " + request.getUrl());

		ResponseDefinition responseDef = stubMappings.serveFor(request);
		if (!responseDef.wasConfigured() && request.isBrowserProxyRequest() && browserProxyingEnabled) {
			return ResponseDefinition.browserProxy(request);
		}
		
		return responseDef;
	}

}
