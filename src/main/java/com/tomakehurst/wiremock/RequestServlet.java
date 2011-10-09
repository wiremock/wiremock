package com.tomakehurst.wiremock;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestServlet extends HttpServlet {

	private static final long serialVersionUID = -5994955091275953358L;
	
	private static ResponseDefinitions responses;
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestMethod method = RequestMethod.valueOf(request.getMethod().toUpperCase());
		ResponseDefinition responseDefinition = responses.get(method, request.getRequestURI());
		response.setStatus(responseDefinition.getStatusCode());
		response.getWriter().write(responseDefinition.getBodyContent());
		
	}

	public static void setResponseDefinitions(ResponseDefinitions responseDefinitions) {
		RequestServlet.responses = responseDefinitions;
	}
}
