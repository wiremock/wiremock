package com.github.tomakehurst.wiremock.mapping;

import java.util.concurrent.atomic.AtomicReference;

public class Scenario {

	public static final String STARTED = "Started";
	
	private final AtomicReference<String> state;

	public Scenario(String currentState) {
		state = new AtomicReference<String>(currentState);
	}
	
	public static Scenario inStartedState() {
		return new Scenario(STARTED);
	}
	
	public String getState() {
		return state.get();
	}
	
	public void setState(String newState) {
		state.set(newState);
	}
	
	public boolean stateIs(String state) {
		return getState().equals(state);
	}

	@Override
	public String toString() {
		return "Scenario [currentState=" + state.get() + "]";
	}
}
