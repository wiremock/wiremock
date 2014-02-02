package com.github.tomakehurst.wiremock.jsontemplate;

/**
 * Base class for all exceptions in this package.
 * 
 */
@SuppressWarnings("serial")
public class JSONTemplateError extends RuntimeException {

	public JSONTemplateError() {
		super();
	}

	public JSONTemplateError(String message, Throwable cause) {
		super(message, cause);
	}

	public JSONTemplateError(String message) {
		super(message);
	}

	public JSONTemplateError(Throwable cause) {
		super(cause);
	}

}
