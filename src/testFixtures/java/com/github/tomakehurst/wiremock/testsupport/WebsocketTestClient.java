package com.github.tomakehurst.wiremock.testsupport;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class WebsocketTestClient {

    private final WebSocketContainer websocketClient = ContainerProvider.getWebSocketContainer();

    private final NotificationCapturingEndpoint endpoint = new NotificationCapturingEndpoint();

    public <T> T withWebsocketSession(String url, Function<Session, T> work) {
        ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
        URI uri = URI.create(url);
        try (Session session = websocketClient.connectToServer(endpoint, endpointConfig, uri)) {
            Thread.sleep(100);
            return work.apply(session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getMessages() {
        return endpoint.messages;
    }

    public String waitForMessage(Predicate<String> predicate) {
        await().atMost(5, SECONDS).until(() -> endpoint.messages.stream().anyMatch(predicate));
        return endpoint.messages.stream().filter(predicate).findFirst().get();
    }

    public static class NotificationCapturingEndpoint extends Endpoint implements MessageHandler.Whole<String> {

        public final List<String> messages = new LinkedList<>();

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            session.addMessageHandler(this);
        }

        @Override
        public void onMessage(String message) {
            messages.add(message);
        }
    }
}
