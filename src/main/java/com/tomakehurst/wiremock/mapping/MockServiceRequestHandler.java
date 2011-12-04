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

import com.tomakehurst.wiremock.servlet.ResponseRenderer;

public class MockServiceRequestHandler extends AbstractRequestHandler {
	
	private Mappings mappings;

	public MockServiceRequestHandler(Mappings mappings, ResponseRenderer responseRenderer) {
		super(responseRenderer);
		this.mappings = mappings;
	}
	
	@Override
	public ResponseDefinition handleRequest(Request request) {
		ResponseDefinition response = mappings.getFor(request);
		return response;
	}

}
