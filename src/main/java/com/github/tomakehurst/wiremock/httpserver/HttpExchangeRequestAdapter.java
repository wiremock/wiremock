package com.github.tomakehurst.wiremock.httpserver;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.ByteStreams.toByteArray;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.mapping.Request;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HttpExchangeRequestAdapter implements Request {
	
	private HttpExchange httpExchange;

	public HttpExchangeRequestAdapter(HttpExchange httpExchange) {
		this.httpExchange = httpExchange;
	}

	@Override
	public String getUrl() {
		return httpExchange.getRequestURI().toString();
	}

	@Override
	public String getAbsoluteUrl() {
		return getUrl();
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.valueOf(httpExchange.getRequestMethod());
	}

	@Override
	public String getHeader(final String key) {
		Headers headers = httpExchange.getRequestHeaders();
		
		List<String> values = find(headers.entrySet(), new Predicate<Map.Entry<String, List<String>>>() {
			public boolean apply(Entry<String, List<String>> input) {
				return input.getKey().toLowerCase().equals(key.toLowerCase());
			}
		}).getValue();
		
		if (values != null && !values.isEmpty()) {
			return values.get(0);
		}
		
		return null;
	}

	@Override
	public boolean containsHeader(String key) {
		return getHeader(key) != null;
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		Headers headers = httpExchange.getRequestHeaders();
		
		return newHashSet(transform(headers.entrySet(), new Function<Map.Entry<String, List<String>>, String>() {
			public String apply(Entry<String, List<String>> input) {
				return input.getKey();
			}
		}));
	}

	@Override
	public String getBodyAsString() {
		try {
			return new String(toByteArray(httpExchange.getRequestBody()), UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isBrowserProxyRequest() {
		return false;
	}

	
}
