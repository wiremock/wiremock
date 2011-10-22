package com.tomakehurst.wiremock.servlet;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.common.io.CharStreams;
import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;

public class HttpServletRequestAdapter implements Request {
	
	private HttpServletRequest request;
	
	public HttpServletRequestAdapter(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getUrl() {
		if (!isNullOrEmpty(request.getContextPath())) {
			return request.getRequestURI().replace(request.getContextPath(), "");
		}
		
		return request.getRequestURI();
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.valueOf(request.getMethod().toUpperCase());
	}

	@Override
	public String getBodyAsString() {
		try {
			return CharStreams.toString(request.getReader());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public String getHeader(String key) {
		return request.getHeader(key);
	}

	@Override
	public boolean containsHeader(String key) {
		return request.getHeader(key) != null;
	}

}
