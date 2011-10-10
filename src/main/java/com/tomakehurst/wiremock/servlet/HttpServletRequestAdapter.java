package com.tomakehurst.wiremock.servlet;

import javax.servlet.http.HttpServletRequest;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.Request;

public class HttpServletRequestAdapter implements Request {
	
	private HttpServletRequest request;
	
	public HttpServletRequestAdapter(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getUri() {
		return request.getRequestURI();
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.valueOf(request.getMethod().toUpperCase());
	}

}
