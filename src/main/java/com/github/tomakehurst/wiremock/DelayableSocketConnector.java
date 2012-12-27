package com.github.tomakehurst.wiremock;

import org.mortbay.jetty.bio.SocketConnector;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayableSocketConnector extends SocketConnector {

    private AtomicInteger count = new AtomicInteger(0);

    private final SocketControl socketControl;

    public DelayableSocketConnector(SocketControl socketControl) {
        this.socketControl = socketControl;
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
        int currentCount = count.incrementAndGet();
        String thread = Thread.currentThread().getName();
        System.out.println("Accepting " + currentCount + " in " + thread);

//        super.accept(acceptorID);
        Socket socket = _serverSocket.accept();
        socketControl.delayIfRequired();
        configure(socket);

        Connection connection=new Connection(socket);
        connection.dispatch();
        System.out.println("Accepted " + currentCount + " in " + thread + " on remote port " + socket.getPort());
    }
}
