package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class NotFoundHandler extends ErrorHandler {

    private final ErrorHandler DEFAULT_HANDLER = new ErrorHandler();

    @Override
    public void handle(String target, final Request baseRequest, final HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.getStatus() == 404) {
            ServletContext adminContext = request.getServletContext().getContext("/__admin");
            Dispatcher requestDispatcher = (Dispatcher) adminContext.getRequestDispatcher("/not-matched");

            try {
                requestDispatcher.error(request, response);
            } catch (ServletException e) {
                throwUnchecked(e);
            }
        } else {
            DEFAULT_HANDLER.handle(target, baseRequest, request, response);
        }
    }
}
