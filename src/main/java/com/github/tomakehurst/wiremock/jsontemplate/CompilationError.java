package com.github.tomakehurst.wiremock.jsontemplate;

/**
 * Base class for errors that happen during the compilation stage.
 *
 */
@SuppressWarnings("serial")
public class CompilationError extends JSONTemplateError {

	public CompilationError() {
		super();
	}

	public CompilationError(String message, Throwable cause) {
		super(message, cause);
	}

	public CompilationError(String message) {
		super(message);
	}

	public CompilationError(Throwable cause) {
		super(cause);
	}

}
