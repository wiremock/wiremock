package com.tomakehurst.wiremock.servlet;

import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.mapping.Response;
import com.tomakehurst.wiremock.mapping.ResponseDefinition;


public interface ResponseRenderer {
	
	public static final String CONTEXT_KEY = "ResponseRenderer";

	void render(ResponseDefinition response, HttpServletResponse httpServletResponse);
	Response render(ResponseDefinition responseDefinition);
	
}
