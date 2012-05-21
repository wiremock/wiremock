package com.github.tomakehurst.wiremock.httpserver;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.Timer;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.github.tomakehurst.wiremock.mapping.RequestHandler;
import com.github.tomakehurst.wiremock.mapping.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DispatchingHandler implements HttpHandler {
	
	private final RequestHandler requestHandler;
	private final Notifier notifier;
	
	public DispatchingHandler(RequestHandler requestHandler, Notifier notifier) {
		this.requestHandler = requestHandler;
		this.notifier = notifier;
	}

	@Override
	public void handle(HttpExchange http) throws IOException {
		long start = System.nanoTime();
		LocalNotifier.set(notifier);
		
		Request request = new HttpExchangeRequestAdapter(http);
		Response response = requestHandler.handle(request);
		
		if (response.wasConfigured()) {
		    response.applyTo(http);
		} else {
			http.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
			http.close();
		}
		
		System.out.println(String.format("DispatchingHandler.handle(): %sms", Timer.millisecondsFrom(start)));
	}

}
