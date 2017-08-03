package com.github.tomakehurst.wiremock.jetty9.sse;

/**
 * @author Christopher Holomek
 */
public enum SseMessage {
	MAPPINGS("mappings"),
	UNMATCHED("unmatched"),
	MATCHED("matched"),
	RECORDING("recording");

	private final String message;

	SseMessage(final String message){
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
