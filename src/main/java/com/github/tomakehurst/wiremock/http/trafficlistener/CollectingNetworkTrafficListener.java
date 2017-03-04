package com.github.tomakehurst.wiremock.http.trafficlistener;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class CollectingNetworkTrafficListener implements WiremockNetworkTrafficListener {
    private final StringBuilder requestBuilder = new StringBuilder();
    private final StringBuilder responseBuilder = new StringBuilder();

    private final Charset charset = Charset.forName("UTF-8");
    private final CharsetDecoder decoder = charset.newDecoder();

    @Override
    public void opened(Socket socket) {
    }

    @Override
    public void incoming(Socket socket, ByteBuffer bytes) {
        try {
            requestBuilder.append(decoder.decode(bytes));
        } catch (CharacterCodingException e) {
            notifier().error("Problem decoding network traffic", e);
        }
    }

    @Override
    public void outgoing(Socket socket, ByteBuffer bytes) {
        try {
            responseBuilder.append(decoder.decode(bytes));
        } catch (CharacterCodingException e) {
            notifier().error("Problem decoding network traffic", e);
        }
    }

    @Override
    public void closed(Socket socket) {
    }

    public String getAllRequests() {
        return requestBuilder.toString();
    }

    public String getAllResponses() {
        return responseBuilder.toString();
    }
}
