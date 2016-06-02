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

import com.github.tomakehurst.wiremock.stubbing.ServedStub;

import java.util.List;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractRequestHandler implements RequestHandler, RequestEventSource {

	protected List<RequestListener> listeners = newArrayList();
	protected final ResponseRenderer responseRenderer;

	public AbstractRequestHandler(ResponseRenderer responseRenderer) {
		this.responseRenderer = responseRenderer;
	}

	@Override
	public void addRequestListener(RequestListener requestListener) {
		listeners.add(requestListener);
	}

	@Override
	public Response handle(Request request) {
		ServedStub servedStub = handleRequest(request);
		ResponseDefinition responseDefinition = servedStub.getResponseDefinition();
		responseDefinition.setOriginalRequest(request);
		Response response = responseRenderer.render(responseDefinition);

		if (logRequests()) {
			notifier().info("Request received:\n" +
					formatRequest(request) +
					"\n\nMatched response definition:\n" + responseDefinition +
					"\n\nResponse:\n" + response);
		}

		for (RequestListener listener: listeners) {
			listener.requestReceived(request, response);
		}

		return response;
	}

	private static String formatRequest(Request request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getClientIp())
				.append(" - ")
				.append(request.getMethod())
				.append(" ")
				.append(request.getUrl());

		if (request.isBrowserProxyRequest()) {
			sb.append(" (via browser proxy request)");
		}

		sb.append("\n\n");
		sb.append(request.getHeaders());

		if (request.getBody() != null) {
			sb.append(request.getBodyAsString()).append("\n");
		}

		return sb.toString();
	}

	protected boolean logRequests() { return false; }

	protected abstract ServedStub handleRequest(Request request);
}
