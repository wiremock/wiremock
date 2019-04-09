package com.github.tomakehurst.wiremock.client;

import java.lang.reflect.Proxy;

import javax.ws.rs.core.HttpHeaders;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

class InvocationMappingBuilder<T> extends BasicMappingBuilder {

	public InvocationMappingBuilder(final Class<T> resource, final ResourceInvocation<T> invocation) {
		super(InvocationMappingBuilder.requestMatcher(resource, invocation));
	}

	private static <T> RequestPatternBuilder requestMatcher(final Class<T> resource,
			final ResourceInvocation<T> invocation) {
		final RecordingInvocationHandler handler = new RecordingInvocationHandler();

		@SuppressWarnings("unchecked")
		final T recordingProxy = (T) Proxy.newProxyInstance(resource.getClassLoader(), new Class[] { resource },
				handler);

		invocation.invoke(recordingProxy);

		final RequestPatternBuilder pb = new RequestPatternBuilder(handler.getRequestMethod(), handler.getUrlPattern()) //
				.withHeader(HttpHeaders.CONTENT_TYPE, handler.getRequestContentType());

		return pb;
	}
}
