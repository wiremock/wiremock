package com.github.tomakehurst.wiremock.jetty9.sse;

import org.eclipse.jetty.servlets.EventSource;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

/**
 * @author Christopher Holomek
 */
public class SseEventSource implements EventSource {

	private Emitter emitter;
	private final ConcurrentLinkedQueue<SseEventSourceListener> listeners;

	SseEventSource(){
		this.listeners = new ConcurrentLinkedQueue<>();
	}

	void sendMessage(final String message){
		if (this.emitter == null) {
			return;
		}
		try {
			this.emitter.data(message);
		} catch (IOException e) {
			notifier().error("Could not send sse message", e);
		}
	}

	void addListener(final SseEventSourceListener listener){
		this.listeners.add(listener);
	}

	void removeListener(final SseEventSourceListener listener){
		this.listeners.remove(listener);
	}

	@Override
	public void onOpen(final Emitter emitter) throws IOException {
		this.emitter = emitter;
	}

	@Override
	public void onClose() {
		for (final SseEventSourceListener listener : listeners) {
			listener.onClose(this);
		}
	}
}
