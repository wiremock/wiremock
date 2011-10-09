package com.tomakehurst.wiremock;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestServlet extends HttpServlet {

	private static final long serialVersionUID = -5994955091275953358L;
	
	private static Responses responses;
	
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		Request request = createRequestFrom(httpServletRequest);
		Response response = responses.getFor(request);
		httpServletResponse.setStatus(response.getStatusCode());
		httpServletResponse.getWriter().write(response.getBodyContent());
		
	}

	public static void setResponseDefinitions(Responses responseDefinitions) {
		RequestServlet.responses = responseDefinitions;
	}
	
	private Request createRequestFrom(HttpServletRequest httpServletRequest) {
		return new Request(RequestMethod.valueOf(httpServletRequest.getMethod()), httpServletRequest.getRequestURI());
	}
}
