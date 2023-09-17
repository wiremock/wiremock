package org.wiremock.grpc.client;

import com.example.grpc.GreetingServiceGrpc;
import io.grpc.Channel;

public class GreetingsClient {

    private final GreetingServiceGrpc.GreetingServiceBlockingStub stub;

    public GreetingsClient(Channel channel) {
        stub = GreetingServiceGrpc.newBlockingStub(channel);
    }

    public String greet(String name) {
        return stub.greeting(com.example.grpc.HelloRequest.newBuilder().setName(name).build()).getGreeting();
    }
}
