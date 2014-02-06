package com.github.tomakehurst.wiremock.jsontemplate;
/**
 * A bad formatter was specified, e.g. {variable|BAD}
 * 
 * 
 */
@SuppressWarnings("serial")
public class BadFormatterError extends CompilationError {

	public BadFormatterError() {
		super();
	}

	public BadFormatterError(String message, Throwable cause) {
		super(message, cause);
	}

	public BadFormatterError(String message) {
		super(message);
	}

	public BadFormatterError(Throwable cause) {
		super(cause);
	}

}
