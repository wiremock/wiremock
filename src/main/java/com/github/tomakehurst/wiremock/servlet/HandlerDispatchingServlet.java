/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.Response;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.base.Charsets.UTF_8;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.URLDecoder.decode;

public class HandlerDispatchingServlet extends HttpServlet {

	public static final String SHOULD_FORWARD_TO_FILES_CONTEXT = "shouldForwardToFilesContext";

	private static final long serialVersionUID = -6602042274260495538L;
	
	private RequestHandler requestHandler;
	private Notifier notifier;
	private String wiremockFileSourceRoot = "/";
	private boolean shouldForwardToFilesContext;
	
	@Override
	public void init(ServletConfig config) {
	    ServletContext context = config.getServletContext();
	    shouldForwardToFilesContext = getFileContextForwardingFlagFrom(config);
	    
	    if (context.getInitParameter("WireMockFileSourceRoot") != null) {
	        wiremockFileSourceRoot = context.getInitParameter("WireMockFileSourceRoot");
	    }
		
		String handlerClassName = config.getInitParameter(RequestHandler.HANDLER_CLASS_KEY);
		context.log(RequestHandler.HANDLER_CLASS_KEY + " from context returned " + handlerClassName);
		requestHandler = (RequestHandler) context.getAttribute(handlerClassName);
		notifier = (Notifier) context.getAttribute(Notifier.KEY);
	}
	
	private boolean getFileContextForwardingFlagFrom(ServletConfig config) {
		String flagValue = config.getInitParameter(SHOULD_FORWARD_TO_FILES_CONTEXT);
		return Boolean.valueOf(flagValue);
	}

	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		LocalNotifier.set(notifier);
		
		Request request = new HttpServletRequestAdapter(httpServletRequest);
        notifier.info("Received request: " + httpServletRequest.toString());

		Response response = requestHandler.handle(request);
		if (response.wasConfigured()) {
		    response.applyTo(httpServletResponse);
		} else if (request.getMethod() == GET && shouldForwardToFilesContext) {
		    forwardToFilesContext(httpServletRequest, httpServletResponse, request);
		} else {
			httpServletResponse.sendError(HTTP_NOT_FOUND);
		}
	}

    private void forwardToFilesContext(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Request request) throws ServletException, IOException {
        String forwardUrl = wiremockFileSourceRoot + WireMockApp.FILES_ROOT + request.getUrl();
        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(decode(forwardUrl, UTF_8.name()));
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }
}
