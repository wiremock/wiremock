package com.github.tomakehurst.wiremock.jsontemplate;


@SuppressWarnings("serial")
public class UndefinedVariable extends EvaluationError {

	public UndefinedVariable() {
		super();
	}

	public UndefinedVariable(String message, Throwable cause) {
		super(message, cause);
	}

	public UndefinedVariable(String message) {
		super(message);
	}

	public UndefinedVariable(Throwable cause) {
		super(cause);
	}

}
