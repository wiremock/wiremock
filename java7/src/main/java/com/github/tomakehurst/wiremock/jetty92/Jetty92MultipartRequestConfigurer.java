package com.github.tomakehurst.wiremock.jetty92;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public class Jetty92MultipartRequestConfigurer implements MultipartRequestConfigurer {

    @Override
    public void configure(HttpServletRequest request) {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement((String)null);
        request.setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
        try {
            InputStream inputStream = request.getInputStream();
            MultiPartInputStreamParser inputStreamParser = new MultiPartInputStreamParser(inputStream, request.getContentType(), null, null);
            request.setAttribute("org.eclipse.jetty.multiPartInputStream", inputStreamParser);
        } catch (IOException e) {
            Exceptions.throwUnchecked(e);
        }
    }
}
