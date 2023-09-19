package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wiremock.grpc.client.GreetingsClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GrpcAcceptanceTest {

    GreetingsClient greetingsClient;

    @RegisterExtension
    public static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig()
                .dynamicPort()
                .globalTemplating(true)
                .withRootDirectory("src/test/resources/wiremock")
//                .extensions(new GrpcExtensionFactory())
        ).build();

    @BeforeEach
    void init() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", wm.getPort())
                .usePlaintext()
                .build();
        greetingsClient = new GreetingsClient(channel);
    }

    @Test
    void shouldReturnGreeting() {
        wm.stubFor(post(urlPathEqualTo("/com.example.grpc.GreetingService/greeting")).willReturn(okJson("{\n" +
                "    \"greeting\": \"Hello {{jsonPath request.body '$.name'}}\"\n" +
                "}")));

        String greeting = greetingsClient.greet("Tom");

        assertThat(greeting, is("Hello Tom"));
    }
}
