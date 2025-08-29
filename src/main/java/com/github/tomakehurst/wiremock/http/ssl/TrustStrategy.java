/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.ssl;

import java.net.Socket;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;

/** The interface Trust strategy. */
public interface TrustStrategy {

  /**
   * Is trusted boolean.
   *
   * @param chain the chain
   * @param authType the auth type
   * @return the boolean
   */
  boolean isTrusted(X509Certificate[] chain, String authType);

  /**
   * Is trusted boolean.
   *
   * @param chain the chain
   * @param authType the auth type
   * @param socket the socket
   * @return the boolean
   */
  boolean isTrusted(X509Certificate[] chain, String authType, Socket socket);

  /**
   * Is trusted boolean.
   *
   * @param chain the chain
   * @param authType the auth type
   * @param engine the engine
   * @return the boolean
   */
  boolean isTrusted(X509Certificate[] chain, String authType, SSLEngine engine);
}
