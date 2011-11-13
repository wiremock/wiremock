package com.tomakehurst.wiremock.servlet;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.mapping.Response;


public interface ResponseRenderer {
	
	public static final String CONTEXT_KEY = "ResponseRenderer";

	void render(Response response, HttpServletResponse httpServletResponse);
	
}
