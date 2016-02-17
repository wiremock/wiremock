package com.github.tomakehurst.wiremock.common;

import java.io.IOException;
import java.net.ServerSocket;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class NetworkUtils {

    public static int acquireRandomPort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException e) {
            return throwUnchecked(e, Integer.class);
        }
    }
}
