package org.wiremock.grpc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class GrpcAcceptanceTest {

    @RegisterExtension
    public static WireMockExtension wm = WireMockExtension.newInstance()
        .options(wireMockConfig()
                .port(8080)
                .httpsPort(8433)
                .httpServerFactory(new GrpcExtension()))
        .build();

    @Test
    void shouldReturnGreeting() throws Exception {
        System.out.println(wm.getPort());

        while (true) {
            Thread.sleep(1000);
        }
    }
}
