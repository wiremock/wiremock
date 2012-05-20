package com.github.tomakehurst.wiremock.httpserver;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.github.tomakehurst.wiremock.mapping.RequestHandler;
import com.github.tomakehurst.wiremock.mapping.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DispatchingHandler implements HttpHandler {
	
	private RequestHandler requestHandler;
	private Notifier notifier;
	
	public DispatchingHandler(RequestHandler requestHandler, Notifier notifier) {
		this.requestHandler = requestHandler;
		this.notifier = notifier;
	}

	@Override
	public void handle(HttpExchange http) throws IOException {
		LocalNotifier.set(notifier);
		
		Request request = new HttpExchangeRequestAdapter(http);
		Response response = requestHandler.handle(request);
		
		if (response.wasConfigured()) {
		    response.applyTo(http);
		} else {
			http.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
			http.close();
		}
	}

}
