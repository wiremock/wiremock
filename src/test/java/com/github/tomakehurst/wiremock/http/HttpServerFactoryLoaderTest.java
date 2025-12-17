/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.extension.StaticExtensionLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class HttpServerFactoryLoaderTest {

  Options options = wireMockConfig();
  Extensions extensions = mock(Extensions.class);

  @SuppressWarnings("unchecked")
  Supplier<Stream<HttpServerFactory>> serviceLoader = mock(Supplier.class);

  @Test
  void loadsExtensionWhenOneIsPresentAndOptionsHasNoHttpServerConfigured() {

    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));

    HttpServerFactory result = loadHttpServerFactory(options);

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void loadsTheNonStandardExtensionWhenMoreThanOneIsPresent() {
    options = wireMockConfig().extensionScanningEnabled(false);

    serverFactoriesAsExtensions(
        List.of(new DefaultHttpServerFactory(), new CustomHttpServerFactory()));

    HttpServerFactory result = loadHttpServerFactory(options);

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheServiceLoaderWhenNoExtensionsArePresentAndOptionsHasNoHttpServerConfigured() {
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory()));

    HttpServerFactory result = loadHttpServerFactory(options);

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void loadsTheNonStandardFactoryViaTheServiceLoaderWhenMoreThanOneIsPresent() {
    serverFactoriesFromServiceLoader(
        List.of(new DefaultHttpServerFactory(), new CustomHttpServerFactory()));

    HttpServerFactory result = loadHttpServerFactory(options);

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheFactoryFromTheOptionsEvenWhenExtensionsAndServicesArePresent() {
    // expect
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));
    assertThat(loadHttpServerFactory(options), instanceOf(CustomHttpServerFactory2.class));

    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    assertThat(loadHttpServerFactory(options), instanceOf(CustomHttpServerFactory.class));

    // when
    Options config = wireMockConfig().httpServerFactory(new CustomHttpServerFactory());
    HttpServerFactory result = loadHttpServerFactory(config);

    // then
    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void throwsDescriptiveExceptionWhenNoSuitableServerFactoryIsFound() {
    serverFactoriesAsExtensions(Collections.emptyList());
    serverFactoriesFromServiceLoader(Collections.emptyList());

    var exception = assertThrows(FatalStartupException.class, () -> loadHttpServerFactory(options));
    assertThat(
        exception.getMessage(),
        equalTo(
            "No suitable HttpServerFactory was found. Please ensure that the classpath includes a WireMock extension that provides an HttpServerFactory implementation. See https://wiremock.org/docs/extending-wiremock/ for more information."));
  }

  private HttpServerFactory loadHttpServerFactory(Options options) {
    return new StaticExtensionLoader<>(HttpServerFactory.class)
        .setSpecificInstance(options.httpServerFactory())
        .setExtensions(extensions)
        .setServiceLoader(serviceLoader)
        .load();
  }

  private void serverFactoriesAsExtensions(List<HttpServerFactory> extensionList) {
    final Map<String, HttpServerFactory> extensionMap =
        extensionList.stream()
            .collect(Collectors.toMap(HttpServerFactory::getName, factory -> factory));
    when(extensions.ofType(HttpServerFactory.class)).thenReturn(extensionMap);
  }

  private void serverFactoriesFromServiceLoader(List<HttpServerFactory> factories) {
    when(serviceLoader.get()).thenReturn(factories.stream());
  }

  public static class CustomHttpServerFactory implements HttpServerFactory {

    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler,
        com.github.tomakehurst.wiremock.websocket.MessageChannels messageChannels,
        com.github.tomakehurst.wiremock.websocket.message.MessageStubMappings messageStubMappings) {
      return null;
    }
  }

  public static class CustomHttpServerFactory2 implements HttpServerFactory {
    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler,
        com.github.tomakehurst.wiremock.websocket.MessageChannels messageChannels,
        com.github.tomakehurst.wiremock.websocket.message.MessageStubMappings messageStubMappings) {
      return null;
    }
  }

  public static class DefaultHttpServerFactory implements HttpServerFactory, DefaultFactory {
    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler,
        com.github.tomakehurst.wiremock.websocket.MessageChannels messageChannels,
        com.github.tomakehurst.wiremock.websocket.message.MessageStubMappings messageStubMappings) {
      return null;
    }
  }
}
