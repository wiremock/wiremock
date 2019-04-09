package com.github.tomakehurst.wiremock.client;

public interface ResourceInvocation<T> {
	public void invoke(T r);
}
