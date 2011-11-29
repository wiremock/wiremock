package com.tomakehurst.wiremock.common;

public interface Notifier {
	
	public static final String KEY = "Notifier";

	void info(String message);
	void error(String message);
	void error(String message, Throwable t);
}
