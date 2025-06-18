/*
 * Copyright (C) 2013-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResource;

import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import java.util.Arrays;

public class HttpsSettings {

  private final int port;
  private final String keyStorePath;
  private final String keyStorePassword;
  private final String keyManagerPassword;
  private final String keyStoreType;
  private final String trustStorePath;
  private final String trustStorePassword;
  private final String trustStoreType;
  private final String[] cipherSuites;
  private final String[] tlsProtocols;
  private final boolean needClientAuth;

  public HttpsSettings(
      int port,
      String keyStorePath,
      String keyStorePassword,
      String keyManagerPassword,
      String keyStoreType,
      String trustStorePath,
      String trustStorePassword,
      String trustStoreType,
      String[] cipherSuites,
      String[] tlsProtocols,
      boolean needClientAuth) {
    this.port = port;
    this.keyStorePath = keyStorePath;
    this.keyStorePassword = keyStorePassword;
    this.keyManagerPassword = keyManagerPassword;
    this.keyStoreType = keyStoreType;
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
    this.trustStoreType = trustStoreType;
    this.cipherSuites = cipherSuites;
    this.tlsProtocols = tlsProtocols;
    this.needClientAuth = needClientAuth;
  }

  public int port() {
    return port;
  }

  public String keyStorePath() {
    return keyStorePath;
  }

  public String keyStorePassword() {
    return keyStorePassword;
  }

  public String keyManagerPassword() {
    return keyManagerPassword;
  }

  public String keyStoreType() {
    return keyStoreType;
  }

  public boolean enabled() {
    return port > -1;
  }

  public String trustStorePath() {
    return trustStorePath;
  }

  public String trustStorePassword() {
    return trustStorePassword;
  }

  public String trustStoreType() {
    return trustStoreType;
  }

  public String[] cipherSuites() {
    return cipherSuites;
  }

  public String[] tlsProtocols() {
    return tlsProtocols;
  }

  public boolean needClientAuth() {
    return needClientAuth;
  }

  public boolean hasTrustStore() {
    return trustStorePath != null;
  }

  public boolean hasCipherSuites() {
    return cipherSuites != null;
  }

  public boolean hasTlsProtocols() {
    return tlsProtocols != null;
  }

  public KeyStoreSettings trustStore() {
    return trustStorePath != null
        ? new KeyStoreSettings(trustStorePath, trustStorePassword, trustStoreType)
        : KeyStoreSettings.NO_STORE;
  }

  public KeyStoreSettings keyStore() {
    return new KeyStoreSettings(keyStorePath, keyStorePassword, keyStoreType);
  }

  @Override
  public String toString() {
    return "HttpsSettings{"
        + "port="
        + port
        + ", keyStorePath='"
        + keyStorePath
        + '\''
        + ", keyStoreType='"
        + keyStoreType
        + '\''
        + ", trustStorePath='"
        + trustStorePath
        + '\''
        + ", trustStoreType='"
        + trustStoreType
        + '\''
        + ", cipherSuites='"
        + Arrays.toString(cipherSuites)
        + '\''
        + ", tlsProtocols='"
        + Arrays.toString(tlsProtocols)
        + '\''
        + ", needClientAuth="
        + needClientAuth
        + '}';
  }

  public static class Builder {

    private int port;
    private String keyStorePath = getResource(Builder.class, "keystore").toString();
    private String keyStorePassword = "password";
    private String keyManagerPassword = "password";
    private String keyStoreType = "JKS";
    private String trustStorePath = null;
    private String trustStorePassword = "password";
    private String trustStoreType = "JKS";
    private String[] cipherSuites = null;
    private String[] tlsProtocols = null;
    private boolean needClientAuth = false;

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder keyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
      return this;
    }

    public Builder keyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
      return this;
    }

    public Builder keyManagerPassword(String keyStorePassword) {
      this.keyManagerPassword = keyStorePassword;
      return this;
    }

    public Builder keyStoreType(String keyStoreType) {
      this.keyStoreType = keyStoreType;
      return this;
    }

    public Builder trustStorePath(String trustStorePath) {
      this.trustStorePath = trustStorePath;
      return this;
    }

    public Builder trustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
      return this;
    }

    public Builder trustStoreType(String trustStoreType) {
      this.trustStoreType = trustStoreType;
      return this;
    }

    public Builder cipherSuites(String... cipherSuites) {
      this.cipherSuites = cipherSuites;
      return this;
    }

    public Builder tlsProtocols(String... tlsProtocols) {
      this.tlsProtocols = tlsProtocols;
      return this;
    }

    public Builder needClientAuth(boolean needClientAuth) {
      this.needClientAuth = needClientAuth;
      return this;
    }

    public HttpsSettings build() {
      return new HttpsSettings(
          port,
          keyStorePath,
          keyStorePassword,
          keyManagerPassword,
          keyStoreType,
          trustStorePath,
          trustStorePassword,
          trustStoreType,
          cipherSuites,
          tlsProtocols,
          needClientAuth);
    }
  }
}
