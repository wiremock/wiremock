package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

public class DefaultMultipartRequestConfigurer implements MultipartRequestConfigurer {

    @Override
    public void configure(HttpServletRequest request) {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String)null);
        request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
    }
}
