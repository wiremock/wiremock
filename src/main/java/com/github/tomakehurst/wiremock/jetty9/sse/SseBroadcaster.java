package com.github.tomakehurst.wiremock.jetty9.sse;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Christopher Holomek
 */
public enum SseBroadcaster implements SseEventSourceListener {
	INSTANCE;

	final ConcurrentLinkedQueue<SseEventSource> sseEventSources;

	SseBroadcaster(){
		this.sseEventSources = new ConcurrentLinkedQueue<>();
	}

	public void sendMessage(final SseMessage message){
		if (message == null) {
			return;
		}
		for (final SseEventSource eventSource : sseEventSources) {
			eventSource.sendMessage(message.getMessage());
		}
	}


	public void register(final SseEventSource sseEventSource) {
		sseEventSource.addListener(this);
		this.sseEventSources.add(sseEventSource);
	}

	@Override
	public void onClose(final SseEventSource sseEventSource) {
		this.sseEventSources.remove(sseEventSource);
		sseEventSource.removeListener(this);
	}
}
