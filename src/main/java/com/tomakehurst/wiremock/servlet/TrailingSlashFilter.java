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

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TrailingSlashFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String path = getRequestPathFrom(httpServletRequest);
        
        StatusAndRedirectExposingHttpServletResponse wrappedResponse = new StatusAndRedirectExposingHttpServletResponse((HttpServletResponse) response);
        chain.doFilter(request, wrappedResponse);
        
        String location = wrappedResponse.getLocation();
        if (wrappedResponse.getStatus() == HTTP_MOVED_TEMP && location.contains(path)) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(location);
            dispatcher.forward(request, response);
        }
    }
    
    private static class StatusAndRedirectExposingHttpServletResponse extends HttpServletResponseWrapper {
        
        private int status;
        private String location;
        
        public StatusAndRedirectExposingHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.location = location;
            super.sendRedirect(location);
        }

        @Override
        public void setStatus(int sc) {
            status = sc;
            super.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            status = sc;
            super.setStatus(sc, sm);
        }

        public int getStatus() {
            return status;
        }

        public String getLocation() {
            return location;
        }
    }
    
    private String getRequestPathFrom(HttpServletRequest httpServletRequest) throws ServletException {
        try {
            String fullPath = new URI(URLEncoder.encode(httpServletRequest.getRequestURI(), "utf-8")).getPath();
            String pathWithoutContext = fullPath.substring(httpServletRequest.getContextPath().length());
            return URLDecoder.decode(pathWithoutContext, "utf-8");
        } catch (URISyntaxException e) {
            throw new ServletException(e);
        } catch (UnsupportedEncodingException e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    public void destroy() {
    }

}
