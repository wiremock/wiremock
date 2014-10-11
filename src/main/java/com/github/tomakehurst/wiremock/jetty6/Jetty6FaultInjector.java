package com.github.tomakehurst.wiremock.jetty6;

import com.github.tomakehurst.wiremock.core.FaultInjector;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class Jetty6FaultInjector implements FaultInjector {

    private final HttpServletResponse response;

    public Jetty6FaultInjector(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        try {
            ActiveSocket.get().close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void malformedResponseChunk() {
        Socket socket = ActiveSocket.get();
        try {
            response.setStatus(200);
            response.flushBuffer();
            socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void randomDataAndCloseConnection() {
        Socket socket = ActiveSocket.get();
        try {
            socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }
}
