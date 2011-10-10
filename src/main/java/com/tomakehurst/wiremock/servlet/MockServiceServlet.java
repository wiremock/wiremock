package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.ImmutableRequest;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.Response;


public class MockServiceServlet extends HttpServlet {

	private static final long serialVersionUID = -5994955091275953358L;
	
	private static RequestHandler mockServiceRequestHandler;
	
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		Request request = createRequestFrom(httpServletRequest);
		Response response = mockServiceRequestHandler.handle(request);
		response.applyTo(httpServletResponse);
	}

	private Request createRequestFrom(HttpServletRequest httpServletRequest) {
		return new ImmutableRequest(RequestMethod.valueOf(httpServletRequest.getMethod()), httpServletRequest.getRequestURI());
	}

	public static void setMockServiceRequestHandler(
			RequestHandler mockServiceRequestHandler) {
		MockServiceServlet.mockServiceRequestHandler = mockServiceRequestHandler;
	}
}
