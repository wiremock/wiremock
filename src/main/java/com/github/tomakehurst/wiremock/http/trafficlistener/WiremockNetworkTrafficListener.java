package com.github.tomakehurst.wiremock.http.trafficlistener;

import java.net.Socket;
import java.nio.ByteBuffer;

public interface WiremockNetworkTrafficListener {
    void opened(Socket socket);
    void incoming(Socket socket, ByteBuffer bytes);
    void outgoing(Socket socket, ByteBuffer bytes);
    void closed(Socket socket);
}
