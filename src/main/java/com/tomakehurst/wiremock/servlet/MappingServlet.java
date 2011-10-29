package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.mapping.AdminRequestHandler;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.Response;

public class MappingServlet extends HttpServlet {

	private static final long serialVersionUID = -6602042274260495538L;
	
	private RequestHandler mappingRequestHandler;
	
	@Override
	public void init(ServletConfig config) {
		ServletContext context = config.getServletContext();
		mappingRequestHandler = (RequestHandler) context.getAttribute(AdminRequestHandler.CONTEXT_KEY);
	}

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		Request request = new HttpServletRequestAdapter(httpServletRequest);
		Response response = mappingRequestHandler.handle(request);
		response.applyTo(httpServletResponse);
	}
}
