package com.github.tomakehurst.wiremock.jetty9.sse;

/**
 * @author Christopher Holomek
 */
public interface SseEventSourceListener {

	void onClose(final SseEventSource sseEventSource);
}
