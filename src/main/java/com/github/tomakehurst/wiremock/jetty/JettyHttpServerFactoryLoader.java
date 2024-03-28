/*
 * Copyright (C) 2014-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.eclipse.jetty.util.Jetty;

/**
 * Jetty's version-dependent {@link HttpServerFactory} loader that accepts the Jetty's major version
 * into account while creating the factory instance.
 */
public interface JettyHttpServerFactoryLoader {
  /**
   * Jetty's version-dependent {@link HttpServerFactory} loader that accepts the Jetty's major
   * version into account while creating the factory instance.
   *
   * @param jettyMajorVersion Jetty's major version
   * @return non-empty {@link Optional} if the loader support this Jetty version, {@code
   *     Optional.empty()} otherwise.
   */
  Optional<HttpServerFactory> getHttpServerFactory(String jettyMajorVersion);

  /**
   * Create the Jetty's version-dependent {@link HttpServerFactory} instance using Java's {@link
   * ServiceLoader} mechanism or throws an exception if none of the factories could be created.
   *
   * @param jettyVersion Jetty version at runtime
   * @return {@link HttpServerFactory} instance
   */
  static HttpServerFactory create(String jettyVersion) {
    final String[] version = Jetty.VERSION.split("[.]");
    if (version.length == 0 || version[0].isBlank()) {
      throw new IllegalArgumentException(
          "Unrecognized Jetty version: "
              + jettyVersion
              + ". Please make sure the right Jetty dependencies are on the classpath.");
    }

    final String jettyMajorVersion = version[0];
    final ServiceLoader<JettyHttpServerFactoryLoader> loaders =
        ServiceLoader.load(JettyHttpServerFactoryLoader.class);

    Throwable cause = null;
    try {
      for (final JettyHttpServerFactoryLoader loader : loaders) {
        final Optional<HttpServerFactory> factoryOpt =
            loader.getHttpServerFactory(jettyMajorVersion);
        if (factoryOpt.isPresent()) {
          return factoryOpt.get();
        }
      }
    } catch (final ServiceConfigurationError ex) {
      /* only catch this kind of exception, the Jetty's HttpServerFactoryLoader could not be instantiated */
      cause = ex;
    }

    throw new IllegalStateException(
        "Unable to find JettyHttpServerFactoryLoader for Jetty version "
            + jettyMajorVersion
            + " (only Jetty 11/12 are supported at the moment). Please make sure the right Jetty dependencies are on the classpath.",
        cause);
  }
}
