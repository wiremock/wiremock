package com.github.tomakehurst.wiremock.http.trafficlistener;

import java.net.Socket;
import java.nio.ByteBuffer;

public class DoNothingWiremockNetworkTrafficListener implements WiremockNetworkTrafficListener {
    @Override
    public void opened(Socket socket) {
    }

    @Override
    public void incoming(Socket socket, ByteBuffer bytes) {
    }

    @Override
    public void outgoing(Socket socket, ByteBuffer bytes) {
    }

    @Override
    public void closed(Socket socket) {
    }
}
