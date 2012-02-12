package com.github.tomakehurst.wiremock.mapping;

public class Scenario {

	public static final String STARTED = "Started";
	
	private final String currentState;

	public Scenario(String currentState) {
		this.currentState = currentState;
	}
	
	public static Scenario inStartedState() {
		return new Scenario(STARTED);
	}
	
	public String getCurrentState() {
		return currentState;
	}
	
	public boolean currentStateIs(String state) {
		return currentState.equals(state);
	}

	@Override
	public String toString() {
		return "Scenario [currentState=" + currentState + "]";
	}
}
