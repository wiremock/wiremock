package com.tomakehurst.wiremock.verification;

public class VerificationResult {

	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public VerificationResult(int count) {
		this.count = count;
	}

	public VerificationResult() {
	}
}
