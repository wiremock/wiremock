package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.tomakehurst.wiremock.servlet.ResponseRenderer;

public abstract class AbstractRequestHandler implements RequestHandler {

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
		ResponseDefinition responseDefinition = handleRequest(request);
		responseDefinition.setOriginalRequest(request);
		Response response = responseRenderer.render(responseDefinition);
		for (RequestListener listener: listeners) {
			listener.requestReceived(request, response);
		}
		
		return response;
	}
	
	protected abstract ResponseDefinition handleRequest(Request request);
}
