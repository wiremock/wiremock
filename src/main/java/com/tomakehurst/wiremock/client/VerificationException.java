package com.tomakehurst.wiremock.client;

public class VerificationException extends RuntimeException {

	private static final long serialVersionUID = 5116216532516117538L;

	public VerificationException() {
		super();
	}

	public VerificationException(String message) {
		super(message);
	}
}
