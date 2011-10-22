package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.mapping.MockServiceRequestHandler;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.Response;


public class MockServiceServlet extends HttpServlet {

	private static final long serialVersionUID = -5994955091275953358L;
	
	private RequestHandler mockServiceRequestHandler;
	
	@Override
	public void init(ServletConfig config) {
		ServletContext context = config.getServletContext();
		mockServiceRequestHandler = (MockServiceRequestHandler) context.getAttribute(MockServiceRequestHandler.CONTEXT_KEY);
	}
	
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		Request request = new HttpServletRequestAdapter(httpServletRequest);
		Response response = mockServiceRequestHandler.handle(request);
		response.applyTo(httpServletResponse);
	}
}
