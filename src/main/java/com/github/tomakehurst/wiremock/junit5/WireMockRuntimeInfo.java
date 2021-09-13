/*
 * Copyright (C) 2011 Thomas Akehurst
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

public class WireMockRuntimeInfo {

  private final WireMockServer wireMockServer;
  private final WireMock wireMock;

  public WireMockRuntimeInfo(WireMockServer wireMockServer) {
    this.wireMockServer = wireMockServer;
    this.wireMock = new WireMock(wireMockServer);
  }

  public int getHttpPort() {
    return wireMockServer.port();
  }

  public int getHttpsPort() {
    return wireMockServer.httpsPort();
  }

  public boolean isHttpEnabled() {
    return wireMockServer.isHttpEnabled();
  }

  public boolean isHttpsEnabled() {
    return wireMockServer.isHttpsEnabled();
  }

  public String getHttpBaseUrl() {
    return "http://localhost:" + getHttpPort();
  }

  public String getHttpsBaseUrl() {
    return "https://localhost:" + getHttpsPort();
  }

  public WireMock getWireMock() {
    return wireMock;
  }
}
