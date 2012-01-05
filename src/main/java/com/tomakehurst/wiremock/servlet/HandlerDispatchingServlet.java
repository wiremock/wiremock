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
package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.WireMockApp;
import com.tomakehurst.wiremock.common.LocalNotifier;
import com.tomakehurst.wiremock.common.Notifier;
import com.tomakehurst.wiremock.mapping.Request;
import com.tomakehurst.wiremock.mapping.RequestHandler;
import com.tomakehurst.wiremock.mapping.Response;

public class HandlerDispatchingServlet extends HttpServlet {

	private static final long serialVersionUID = -6602042274260495538L;
	
	private RequestHandler requestHandler;
	private Notifier notifier;
	private String wiremockFileSourceRoot = "/";
	
	@Override
	public void init(ServletConfig config) {
	    ServletContext context = config.getServletContext();
	    
	    if (context.getInitParameter("WireMockFileSourceRoot") != null) {
	        wiremockFileSourceRoot = context.getInitParameter("WireMockFileSourceRoot");
	    }
		
		String handlerClassName = config.getInitParameter(RequestHandler.HANDLER_CLASS_KEY);
		context.log(RequestHandler.HANDLER_CLASS_KEY + " from context returned " + handlerClassName);
		requestHandler = (RequestHandler) context.getAttribute(handlerClassName);
		notifier = (Notifier) context.getAttribute(Notifier.KEY);
	}

	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		LocalNotifier.set(notifier);
		
		Request request = new HttpServletRequestAdapter(httpServletRequest);
		Response response = requestHandler.handle(request);
		if (response.wasConfigured()) {
		    response.applyTo(httpServletResponse);
		} else {
		    forwardToFilesContext(httpServletRequest, httpServletResponse, request);
		}
	}

    private void forwardToFilesContext(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Request request) throws ServletException, IOException {
        String forwardUrl = wiremockFileSourceRoot + WireMockApp.FILES_ROOT + request.getUrl();
        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(forwardUrl);
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }
}
