/*
 * Copyright (C) 2024 Thomas Akehurst
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
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.jetty.JettyHttpServerFactory;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class HttpServerFactoryLoaderTest {

  Options options = wireMockConfig();
  Extensions extensions = mock(Extensions.class);
  Supplier<List<HttpServerFactory>> serviceLoader = mock(Supplier.class);

  HttpServerFactoryLoader loader;

  @Test
  void loadsExtensionWhenOneIsPresentAndJettyVersionIs11() {
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, true);

    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void loadsExtensionWhenOneIsPresentAndJettyVersionIsNot11() {
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, false);
    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void loadsTheNonStandardExtensionWhenMoreThanOneIsPresent() {
    options = wireMockConfig().extensionScanningEnabled(false);
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, false);

    serverFactoriesAsExtensions(
        List.of(new DefaultHttpServerFactory(), new CustomHttpServerFactory()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheServiceLoaderWhenNoExtensionsArePresentAndJettyVersionIsNot11() {
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, false);
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheDefaultFactoryWhenNoExtensionsArePresentAndJettyVersionIs11() {
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, true);
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(JettyHttpServerFactory.class));
  }

  @Test
  void loadsTheNonStandardFactoryViaTheServiceLoaderWhenMoreThanOneIsPresent() {
    loader = new HttpServerFactoryLoader(options, extensions, serviceLoader, false);
    serverFactoriesFromServiceLoader(
        List.of(new DefaultHttpServerFactory(), new CustomHttpServerFactory()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheFactoryFromTheOptionsObjectWhenNoExtensionsPresentAndJettyVersionIs11() {
    Options config = wireMockConfig().httpServerFactory(new CustomHttpServerFactory());
    loader = new HttpServerFactoryLoader(config, extensions, serviceLoader, true);
    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  @Test
  void usesTheFactoryFromTheOptionsObjectWhenNoExtensionsPresentAndJettyVersionIsNot11() {
    Options config = wireMockConfig().httpServerFactory(new CustomHttpServerFactory());
    loader = new HttpServerFactoryLoader(config, extensions, serviceLoader, false);
    serverFactoriesAsExtensions(List.of(new CustomHttpServerFactory()));
    serverFactoriesFromServiceLoader(List.of(new CustomHttpServerFactory2()));

    HttpServerFactory result = loader.load();

    assertThat(result, instanceOf(CustomHttpServerFactory.class));
  }

  private void serverFactoriesAsExtensions(List<HttpServerFactory> extensionList) {
    final Map<String, HttpServerFactory> extensionMap =
        extensionList.stream()
            .collect(Collectors.toMap(HttpServerFactory::getName, factory -> factory));
    when(extensions.ofType(HttpServerFactory.class)).thenReturn(extensionMap);
  }

  private void serverFactoriesFromServiceLoader(List<HttpServerFactory> factories) {
    when(serviceLoader.get()).thenReturn(factories);
  }

  public static class CustomHttpServerFactory implements HttpServerFactory {

    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler) {
      return null;
    }
  }

  public static class CustomHttpServerFactory2 implements HttpServerFactory {
    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler) {
      return null;
    }
  }

  public static class DefaultHttpServerFactory implements HttpServerFactory, DefaultFactory {
    @Override
    public HttpServer buildHttpServer(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler) {
      return null;
    }
  }
}
