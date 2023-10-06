package com.github.tomakehurst.wiremock.common;

import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PortManager {

    protected static final ConcurrentMap<Integer, Boolean> usedPorts = new ConcurrentHashMap<>();

    public static int allocatePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();

            if (usedPorts.putIfAbsent(port, true) == null) {
                return port;
            } else {
                return allocatePort();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void releasePort(int port) {
        usedPorts.remove(port);
    }

    public static void clearUsedPorts() {
        usedPorts.clear();
    }
}
