/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

/** The type Wire mock runtime info. */
public class WireMockRuntimeInfo {

  private final WireMockServer wireMockServer;
  private final WireMock wireMock;

  /**
   * Instantiates a new Wire mock runtime info.
   *
   * @param wireMockServer the wire mock server
   */
  public WireMockRuntimeInfo(WireMockServer wireMockServer) {
    this.wireMockServer = wireMockServer;
    this.wireMock = new WireMock(wireMockServer);
  }

  /**
   * Gets http port.
   *
   * @return the http port
   */
  public int getHttpPort() {
    return wireMockServer.port();
  }

  /**
   * Gets https port.
   *
   * @return the https port
   */
  public int getHttpsPort() {
    return wireMockServer.httpsPort();
  }

  /**
   * Is http enabled boolean.
   *
   * @return the boolean
   */
  public boolean isHttpEnabled() {
    return wireMockServer.isHttpEnabled();
  }

  /**
   * Is https enabled boolean.
   *
   * @return the boolean
   */
  public boolean isHttpsEnabled() {
    return wireMockServer.isHttpsEnabled();
  }

  /**
   * Gets http base url.
   *
   * @return the http base url
   */
  public String getHttpBaseUrl() {
    return "http://localhost:" + getHttpPort();
  }

  /**
   * Gets https base url.
   *
   * @return the https base url
   */
  public String getHttpsBaseUrl() {
    return "https://localhost:" + getHttpsPort();
  }

  /**
   * Gets wire mock.
   *
   * @return the wire mock
   */
  public WireMock getWireMock() {
    return wireMock;
  }
}
