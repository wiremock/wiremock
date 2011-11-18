package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContentTypeSettingFilter implements Filter {
    
    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        if (response instanceof HttpServletResponse) {
            String filePath = ((HttpServletRequest) request).getRequestURI();
            String contentType = context.getMimeType(filePath);
            ((HttpServletResponse) response).setContentType(contentType);
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
