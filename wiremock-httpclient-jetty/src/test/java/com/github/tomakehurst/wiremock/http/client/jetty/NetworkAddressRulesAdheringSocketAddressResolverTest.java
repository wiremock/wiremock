/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProhibitedNetworkAddressException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class NetworkAddressRulesAdheringSocketAddressResolverTest {

  private static final int ARBITRARY_PORT = 8080;

  @ParameterizedTest
  @ValueSource(strings = {"google.com", "github.com"})
  void resolveSucceedsWithAllowAllRules(String host) throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.ALLOW_ALL;
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    List<InetSocketAddress> addresses = resolve(resolver, host, ARBITRARY_PORT);

    assertThat(addresses).isNotEmpty();
    assertThat(addresses).allMatch(addr -> addr.getPort() == ARBITRARY_PORT);
  }

  @ParameterizedTest
  @ValueSource(strings = {"evil.com", "badhost.org"})
  void resolveFailsWithDenyRule(String host) throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().deny(host).build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(
        host,
        ARBITRARY_PORT,
        null,
        new TestPromise<>(future));

    assertThat(future)
        .failsWithin(5, TimeUnit.SECONDS)
        .withThrowableOfType(Exception.class)
        .withCauseInstanceOf(ProhibitedNetworkAddressException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"127.0.0.1", "192.168.1.1"})
  void resolveFailsWithIpDenyRule(String host) throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().deny(host).build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(
        host,
        ARBITRARY_PORT,
        null,
        new TestPromise<>(future));

    assertThat(future)
        .failsWithin(5, TimeUnit.SECONDS)
        .withThrowableOfType(Exception.class)
        .withCauseInstanceOf(ProhibitedNetworkAddressException.class);
  }

  @Test
  void resolveSucceedsWithAllowRuleForAllowedHost() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().allow("google.com").build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    List<InetSocketAddress> addresses = resolve(resolver, "google.com", ARBITRARY_PORT);

    assertThat(addresses).isNotEmpty();
  }

  @Test
  void resolveFailsWithAllowRuleForDisallowedHost() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().allow("google.com").build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(
        "github.com",
        ARBITRARY_PORT,
        null,
        new TestPromise<>(future));

    assertThat(future)
        .failsWithin(5, TimeUnit.SECONDS)
        .withThrowableOfType(Exception.class)
        .withCauseInstanceOf(ProhibitedNetworkAddressException.class);
  }

  @Test
  void resolveSucceedsForLocalhostWithNoRules() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    List<InetSocketAddress> addresses = resolve(resolver, "localhost", ARBITRARY_PORT);

    assertThat(addresses).isNotEmpty();
    assertThat(addresses).allMatch(addr -> addr.getPort() == ARBITRARY_PORT);
  }

  @Test
  void resolveFailsForUnknownHost() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.ALLOW_ALL;
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(
        "this-host-definitely-does-not-exist-123456789.invalid",
        ARBITRARY_PORT,
        null,
        new TestPromise<>(future));

    assertThat(future)
        .failsWithin(5, TimeUnit.SECONDS)
        .withThrowableOfType(Exception.class);
  }

  @Test
  void resolveReturnsCorrectPort() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.ALLOW_ALL;
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    int customPort = 9999;
    List<InetSocketAddress> addresses = resolve(resolver, "localhost", customPort);

    assertThat(addresses).isNotEmpty();
    assertThat(addresses).allMatch(addr -> addr.getPort() == customPort);
  }

  @Test
  void resolveHandlesWildcardDenyRules() throws Exception {
    NetworkAddressRules rules = NetworkAddressRules.builder().deny("*.example.com").build();
    NetworkAddressRulesAdheringSocketAddressResolver resolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(rules);

    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(
        "sub.example.com",
        ARBITRARY_PORT,
        null,
        new TestPromise<>(future));

    assertThat(future)
        .failsWithin(5, TimeUnit.SECONDS)
        .withThrowableOfType(Exception.class)
        .withCauseInstanceOf(ProhibitedNetworkAddressException.class);
  }

  private List<InetSocketAddress> resolve(
      NetworkAddressRulesAdheringSocketAddressResolver resolver, String host, int port)
      throws Exception {
    CompletableFuture<List<InetSocketAddress>> future = new CompletableFuture<>();
    resolver.resolve(host, port, null, new TestPromise<>(future));
    return future.get(5, TimeUnit.SECONDS);
  }

  private static class TestPromise<T> implements org.eclipse.jetty.util.Promise<T> {
    private final CompletableFuture<T> future;

    TestPromise(CompletableFuture<T> future) {
      this.future = future;
    }

    @Override
    public void succeeded(T result) {
      future.complete(result);
    }

    @Override
    public void failed(Throwable x) {
      future.completeExceptionally(x);
    }
  }
}