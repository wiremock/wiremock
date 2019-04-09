package com.github.tomakehurst.wiremock.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;

class RecordingInvocationHandler implements InvocationHandler {

	private RequestMethod requestMethod;

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (findAnnotation(method, GET.class).isPresent()) {
			this.requestMethod = RequestMethod.GET;
		} else if (findAnnotation(method, POST.class).isPresent()) {
			this.requestMethod = RequestMethod.POST;
		} else if (findAnnotation(method, PUT.class).isPresent()) {
			this.requestMethod = RequestMethod.PUT;
		} else if (findAnnotation(method, DELETE.class).isPresent()) {
			this.requestMethod = RequestMethod.DELETE;
		}
		return null;
	}

	private Optional<Annotation> findAnnotation(final Method method, final Class<?> findAnnotation) {
		final Annotation[] methodAnnotations = method.getAnnotations();
		for (final Annotation annotation : methodAnnotations) {
			if (annotation.annotationType() == findAnnotation) {
				return Optional.of(annotation);
			}
		}
		return Optional.empty();
	}

	public RequestMethod getRequestMethod() {
		return requestMethod;
	}

	public UrlPattern getUrlPattern() {
		// TODO
		return null;
	}

	public StringValuePattern getRequestContentType() {
		// TODO
		return null;
	}

}
