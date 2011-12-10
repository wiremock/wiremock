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
package com.tomakehurst.wiremock.servlet;

import com.tomakehurst.wiremock.mapping.Response;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;

public class BasicResponseRenderer implements ResponseRenderer {

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		Response response = new Response(responseDefinition.getStatus());
		response.addHeaders(responseDefinition.getHeaders());
		response.setBody(responseDefinition.getBody());
		return response;
	}
}
