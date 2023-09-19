/*
 * Copyright (C) 2023 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.grpc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wiremock.grpc.dsl.WireMockGrpc.*;

import com.example.grpc.GreetingServiceGrpc;
import com.example.grpc.HelloResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wiremock.grpc.client.GreetingsClient;
import org.wiremock.grpc.dsl.WireMockGrpcService;

public class GrpcAcceptanceTest {

  WireMockGrpcService greetingMockDsl;
  GreetingsClient greetingsClient;

  @RegisterExtension
  public static WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(
              wireMockConfig()
                  .dynamicPort()
                  .globalTemplating(true)
                  .withRootDirectory("src/test/resources/wiremock")
              //                .extensions(new GrpcExtensionFactory())
              )
          .build();

  @BeforeEach
  void init() {
    greetingMockDsl =
        new WireMockGrpcService(new WireMock(wm.getPort()), GreetingServiceGrpc.SERVICE_NAME);

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", wm.getPort()).usePlaintext().build();
    greetingsClient = new GreetingsClient(channel);
  }

  @Test
  void shouldReturnGreetingBuiltViaTemplatedJson() {
    wm.stubFor(
        post(urlPathEqualTo("/com.example.grpc.GreetingService/greeting"))
            .willReturn(
                okJson(
                    "{\n"
                        + "    \"greeting\": \"Hello {{jsonPath request.body '$.name'}}\"\n"
                        + "}")));

    String greeting = greetingsClient.greet("Tom");

    assertThat(greeting, is("Hello Tom"));
  }

  @Test
  void returnsResponseBuiltFromJson() {
    greetingMockDsl.stubFor(
        method("greeting")
            .willReturn(Status.OK.json("{\n" + "    \"greeting\": \"Hi Tom from JSON\"\n" + "}")));

    String greeting = greetingsClient.greet("Whatever");

    assertThat(greeting, is("Hi Tom from JSON"));
  }

  @Test
  void returnsResponseBuiltFromMessageObject() {
    greetingMockDsl.stubFor(
        method("greeting")
            .willReturn(
                Status.OK.message(HelloResponse.newBuilder().setGreeting("Hi Tom from object"))));

    String greeting = greetingsClient.greet("Whatever");

    assertThat(greeting, is("Hi Tom from object"));
  }

  @Test
  void matchesOnExactRequestByJson() {
    greetingMockDsl.stubFor(
        method("greeting")
            .withRequestMessage(equalToJson("{ \"name\":  \"Tom\" }"))
            .willReturn(Status.OK.message(HelloResponse.newBuilder().setGreeting("OK"))));

    assertThat(greetingsClient.greet("Tom"), is("OK"));

    assertThrows(StatusRuntimeException.class, () -> greetingsClient.greet("Wrong"));
  }
}
