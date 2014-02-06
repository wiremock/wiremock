package com.github.tomakehurst.wiremock.jsontemplate;


@SuppressWarnings("serial")
public class TemplateSyntaxError extends CompilationError {

	public TemplateSyntaxError() {
		super();
	}

	public TemplateSyntaxError(String message, Throwable cause) {
		super(message, cause);
	}

	public TemplateSyntaxError(String message) {
		super(message);
	}

	public TemplateSyntaxError(Throwable cause) {
		super(cause);
	}

}
