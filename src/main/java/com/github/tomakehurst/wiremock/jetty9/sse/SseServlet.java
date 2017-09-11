package com.github.tomakehurst.wiremock.jetty9.sse;

import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Christopher Holomek
 */
public class SseServlet extends EventSourceServlet {

	@Override
	protected EventSource newEventSource(final HttpServletRequest request) {
		SseEventSource sseEventSource = new SseEventSource();

		SseBroadcaster.INSTANCE.register(sseEventSource);

		return sseEventSource;
	}
}
