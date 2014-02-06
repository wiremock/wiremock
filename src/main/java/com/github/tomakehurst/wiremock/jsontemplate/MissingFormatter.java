package com.github.tomakehurst.wiremock.jsontemplate;


@SuppressWarnings("serial")
public class MissingFormatter extends CompilationError {

	public MissingFormatter() {
	}

	public MissingFormatter(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingFormatter(String message) {
		super(message);
	}

	public MissingFormatter(Throwable cause) {
		super(cause);
	}

}
