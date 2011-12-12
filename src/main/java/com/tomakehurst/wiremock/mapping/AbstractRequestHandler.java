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

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.tomakehurst.wiremock.common.DoNothingExceptionHandler;
import com.tomakehurst.wiremock.common.ExceptionHandler;
import com.tomakehurst.wiremock.servlet.ResponseRenderer;

public abstract class AbstractRequestHandler implements RequestHandler {

	protected List<RequestListener> listeners = newArrayList();
	protected ExceptionHandler exceptionHandler;
	protected final ResponseRenderer responseRenderer;
	
	public AbstractRequestHandler(final ResponseRenderer responseRenderer) {
		this.responseRenderer = responseRenderer;
		this.exceptionHandler = new DoNothingExceptionHandler();
	}

	@Override
	public void addRequestListener(final RequestListener requestListener) {
		listeners.add(requestListener);
	}

	@Override
	public Response handle(final Request request) {
		final ResponseDefinition responseDefinition;
		try {
		    responseDefinition = handleRequest(request);
		} catch (final RuntimeException e) {
		    return exceptionHandler.handle(e);
		}
		
		responseDefinition.setOriginalRequest(request);
		final Response response = responseRenderer.render(responseDefinition);
		for (final RequestListener listener: listeners) {
			listener.requestReceived(request, response);
		}
		
		return response;
	}
	
	protected abstract ResponseDefinition handleRequest(Request request);

    public void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
