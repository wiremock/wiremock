package com.github.tomakehurst.wiremock.jsontemplate;

@SuppressWarnings("serial")
public class ConfigurationError extends CompilationError {

	public ConfigurationError() {
		super();
	}

	public ConfigurationError(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationError(String message) {
		super(message);
	}

	public ConfigurationError(Throwable cause) {
		super(cause);
	}

}
