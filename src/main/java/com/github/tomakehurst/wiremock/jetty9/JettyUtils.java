package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.server.Response;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class JettyUtils {

    public static Response unwrapResponse(HttpServletResponse httpServletResponse) {
        if (httpServletResponse instanceof HttpServletResponseWrapper) {
            ServletResponse unwrapped = ((HttpServletResponseWrapper) httpServletResponse).getResponse();
            return (Response) unwrapped;
        }

        return (Response) httpServletResponse;
    }
}
