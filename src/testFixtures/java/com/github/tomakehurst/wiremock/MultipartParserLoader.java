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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.Request;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.eclipse.jetty.util.Jetty;

/**
 * Create the Jetty's version-dependent {@link MultipartParser} instance that accepts the Jetty's
 * major version into account while creating the multipart parser instance.
 */
public interface MultipartParserLoader {
  Optional<MultipartParser> getMultipartParser(String jettyMajorVersion);

  /** The multipart parser implementation that depends on Jetty version being used. */
  interface MultipartParser {
    Collection<Request.Part> parse(byte[] body, String contentType);
  }

  /**
   * Parses the body using {@link MultipartParser} instance.
   *
   * @param body body
   * @param contentType content type
   * @return the list of parsed multipart parts
   */
  static Collection<Request.Part> parts(byte[] body, String contentType) {
    return create(Jetty.VERSION).parse(body, contentType);
  }

  /**
   * Create the Jetty's version-dependent {@link MultipartParser} instance using Java's {@link
   * ServiceLoader} mechanism or throws an exception if none of the multipart parser could be
   * created.
   *
   * @param jettyVersion Jetty version at runtime
   * @return {@link MultipartParser} instance
   */
  static MultipartParser create(String jettyVersion) {
    final String[] version = Jetty.VERSION.split("[.]");
    if (version.length == 0 || version[0].isBlank()) {
      throw new IllegalArgumentException(
          "Unrecognized Jetty version: "
              + jettyVersion
              + ". Please make sure the right Jetty dependencies are on the classpath.");
    }

    final String jettyMajorVersion = version[0];
    final ServiceLoader<MultipartParserLoader> loaders =
        ServiceLoader.load(MultipartParserLoader.class);

    Throwable cause = null;
    try {
      for (final MultipartParserLoader loader : loaders) {
        final Optional<MultipartParser> multipartParserOpt =
            loader.getMultipartParser(jettyMajorVersion);
        if (multipartParserOpt.isPresent()) {
          return multipartParserOpt.get();
        }
      }
    } catch (final ServiceConfigurationError ex) {
      /* only catch this kind of exception, the Jetty's HttpServerFactoryLoader could not be instantiated */
      cause = ex;
    }

    throw new IllegalStateException(
        "Unable to find MultipartParserLoader for Jetty version "
            + jettyMajorVersion
            + " (only Jetty 11/12 are supported at the moment). Please make sure the right Jetty dependencies are on the classpath.",
        cause);
  }
}
