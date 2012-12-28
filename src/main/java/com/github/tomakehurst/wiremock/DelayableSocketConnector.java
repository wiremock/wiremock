package com.github.tomakehurst.wiremock;

import org.mortbay.jetty.bio.SocketConnector;

import java.io.IOException;
import java.net.Socket;

public class DelayableSocketConnector extends SocketConnector {

    private final RequestDelayControl requestDelayControl;

    public DelayableSocketConnector(RequestDelayControl requestDelayControl) {
        this.requestDelayControl = requestDelayControl;
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
        Socket socket = _serverSocket.accept();
        requestDelayControl.delayIfRequired();
        configure(socket);

        Connection connection = new Connection(socket);
        connection.dispatch();
    }
}
