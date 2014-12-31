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

import org.apache.velocity.VelocityContext;

import com.github.tomakehurst.wiremock.core.StubServer;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class StubRequestHandler extends AbstractRequestHandler {
	
	private final StubServer stubServer;
	
	private final VelocityContext velocityContext;

	public StubRequestHandler(StubServer stubServer, ResponseRenderer responseRenderer,
			VelocityContext velocityContext) {
		super(responseRenderer);
		this.stubServer = stubServer;
		this.velocityContext = velocityContext;
	}
	
	@Override
	public ResponseDefinition handleRequest(Request request) {
        notifier().info("Request received:\n" + request);       
        updateTemplateContext(request);
		ResponseDefinition responseDef = stubServer.serveStubFor(request);
		return responseDef;
	}
	
	private void updateTemplateContext(final Request request) {
		velocityContext.put("requestAbsoluteUrl",request.getAbsoluteUrl());
		velocityContext.put("requestUrl",request.getUrl());
		final String requestBody = request.getBodyAsString();
		if(!requestBody.isEmpty() && requestBody != null) {
			velocityContext.put("requestBody",requestBody);
		}        
        velocityContext.put("requestMethod",request.getMethod());
        for(HttpHeader header : request.getHeaders().all()) {
            velocityContext.put("requestHeader".concat(header.key()),
            		header.values().toString());
        }
	}
}