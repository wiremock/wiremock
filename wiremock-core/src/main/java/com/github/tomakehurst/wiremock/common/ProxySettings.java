/*
 * Copyright (C) 2013-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkParameter;
import static com.github.tomakehurst.wiremock.common.Strings.isNotEmpty;
import static org.wiremock.url.SchemeRegistry.http;

import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.IllegalUri;
import org.wiremock.url.Password;
import org.wiremock.url.Uri;

public class ProxySettings {

  public static final ProxySettings NO_PROXY = new ProxySettings(null, 0);
  public static final int DEFAULT_PORT = 80;

  private final String host;
  private final int port;

  private String username;
  private String password;

  public ProxySettings(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public static ProxySettings fromString(String config) {
    try {
      AbsoluteUrl proxyUrl;
      {
        var maybeAbsoluteProxyUrl = Uri.parse(config);
        if (maybeAbsoluteProxyUrl instanceof AbsoluteUrl) {
          proxyUrl = (AbsoluteUrl) maybeAbsoluteProxyUrl;
        } else {
          //noinspection HttpUrlsUsage
          String withScheme = "http://" + config;
          proxyUrl = AbsoluteUrl.parse(withScheme);
        }
      }
      if (!proxyUrl.getScheme().equals(http)) {
        throw new IllegalArgumentException(
            "Proxy via does not support any other protocol than http");
      }
      checkParameter(!proxyUrl.getHost().isEmpty(), "Host part of proxy must be specified");
      ProxySettings proxySettings =
          new ProxySettings(
              proxyUrl.getHost().toString(), proxyUrl.getResolvedPort().getIntValue());
      if (proxyUrl.getUserInfo() != null) {
        proxySettings.setUsername(proxyUrl.getUserInfo().getUsername().toString());
        Password password = proxyUrl.getUserInfo().getPassword();
        if (password != null) {
          proxySettings.setPassword(password.toString());
        }
      }
      return proxySettings;
    } catch (IllegalUri e) {
      throw new IllegalArgumentException(
          String.format("Proxy via Url '%s' was not recognized", config), e);
    }
  }

  public String host() {
    return host;
  }

  public int port() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    if (this == NO_PROXY) {
      return "(no proxy)";
    }

    return String.format(
        "%s:%s%s", host(), port(), (isNotEmpty(this.username) ? " (with credentials)" : ""));
  }
}
