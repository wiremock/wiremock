package com.github.tomakehurst.wiremock.jetty9.websockets;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Christopher Holomek
 */
@ServerEndpoint("/events")
public class WebSocketEndpoint {

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onWebSocketConnect(final Session session) {
        System.out.println("Socket Connected: " + session);

        WebSocketEndpoint.sessions.add(session);
    }

    @OnMessage
    public void onWebSocketText(final String message) {
        System.out.println("Received TEXT message: " + message);
    }

    @OnClose
    public void onWebSocketClose(final CloseReason reason, final Session session) {
        System.out.println("Socket Closed: " + reason);

        WebSocketEndpoint.sessions.remove(session);
    }

    @OnError
    public void onWebSocketError(final Session session, final Throwable cause) {
        cause.printStackTrace(System.err);

        //        WebSocketEndpoint.sessions.remove(session);
    }

    public static void broadcast(final Message message) {
        for (final Session session : WebSocketEndpoint.sessions) {
            synchronized (session) { // we need to synchronize the messages send to client.
                try {
                    session.getBasicRemote().sendText(message.getMessage());
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
