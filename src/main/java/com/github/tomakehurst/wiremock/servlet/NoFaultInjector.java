package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.core.FaultInjector;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class NoFaultInjector implements FaultInjector {

    private final HttpServletResponse httpServletResponse;

    public NoFaultInjector(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        sendError();
    }

    @Override
    public void malformedResponseChunk() {
        sendError();
    }

    @Override
    public void randomDataAndCloseConnection() {
        sendError();
    }

    private void sendError() {
        httpServletResponse.setStatus(418);
        try {
            httpServletResponse.getWriter().write("No fault injector is configured!");
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }
}
