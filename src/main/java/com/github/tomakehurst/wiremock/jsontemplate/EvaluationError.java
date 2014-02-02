package com.github.tomakehurst.wiremock.jsontemplate;

@SuppressWarnings("serial")
public class EvaluationError extends JSONTemplateError {

	public EvaluationError() {
		super();
	}

	public EvaluationError(String message, Throwable cause) {
		super(message, cause);
	}

	public EvaluationError(String message) {
		super(message);
	}

	public EvaluationError(Throwable cause) {
		super(cause);
	}

}
