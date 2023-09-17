package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.wiremock.grpc.client.GreetingsClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GrpcAcceptanceTest {

    @RegisterExtension
    public static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig()
                .dynamicPort()
                .withRootDirectory("src/test/resources/wiremock")
                .extensions(new GrpcExtensionFactory()))
        .build();

    @Test
    void shouldReturnGreeting() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", wm.getPort())
                .usePlaintext()
                .build();
        GreetingsClient client = new GreetingsClient(channel);

        String greeting = client.greet("Tom");

        assertThat(greeting, is("Hi Tom"));
    }
}
