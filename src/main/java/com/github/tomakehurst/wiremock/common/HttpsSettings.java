/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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

/** The type Https settings. */
public class HttpsSettings {

  private final int port;
  private final String keyStorePath;
  private final String keyStorePassword;
  private final String keyManagerPassword;
  private final String keyStoreType;
  private final String trustStorePath;
  private final String trustStorePassword;
  private final String trustStoreType;
  private final boolean needClientAuth;

  /**
   * Instantiates a new Https settings.
   *
   * @param port the port
   * @param keyStorePath the key store path
   * @param keyStorePassword the key store password
   * @param keyManagerPassword the key manager password
   * @param keyStoreType the key store type
   * @param trustStorePath the trust store path
   * @param trustStorePassword the trust store password
   * @param trustStoreType the trust store type
   * @param needClientAuth the need client auth
   */
  public HttpsSettings(
      int port,
      String keyStorePath,
      String keyStorePassword,
      String keyManagerPassword,
      String keyStoreType,
      String trustStorePath,
      String trustStorePassword,
      String trustStoreType,
      boolean needClientAuth) {
    this.port = port;
    this.keyStorePath = keyStorePath;
    this.keyStorePassword = keyStorePassword;
    this.keyManagerPassword = keyManagerPassword;
    this.keyStoreType = keyStoreType;
    this.trustStorePath = trustStorePath;
    this.trustStorePassword = trustStorePassword;
    this.trustStoreType = trustStoreType;
    this.needClientAuth = needClientAuth;
  }

  /**
   * Port int.
   *
   * @return the int
   */
  public int port() {
    return port;
  }

  /**
   * Key store path string.
   *
   * @return the string
   */
  public String keyStorePath() {
    return keyStorePath;
  }

  /**
   * Key store password string.
   *
   * @return the string
   */
  public String keyStorePassword() {
    return keyStorePassword;
  }

  /**
   * Key manager password string.
   *
   * @return the string
   */
  public String keyManagerPassword() {
    return keyManagerPassword;
  }

  /**
   * Key store type string.
   *
   * @return the string
   */
  public String keyStoreType() {
    return keyStoreType;
  }

  /**
   * Enabled boolean.
   *
   * @return the boolean
   */
  public boolean enabled() {
    return port > -1;
  }

  /**
   * Trust store path string.
   *
   * @return the string
   */
  public String trustStorePath() {
    return trustStorePath;
  }

  /**
   * Trust store password string.
   *
   * @return the string
   */
  public String trustStorePassword() {
    return trustStorePassword;
  }

  /**
   * Trust store type string.
   *
   * @return the string
   */
  public String trustStoreType() {
    return trustStoreType;
  }

  /**
   * Need client auth boolean.
   *
   * @return the boolean
   */
  public boolean needClientAuth() {
    return needClientAuth;
  }

  /**
   * Has trust store boolean.
   *
   * @return the boolean
   */
  public boolean hasTrustStore() {
    return trustStorePath != null;
  }

  /**
   * Trust store key store settings.
   *
   * @return the key store settings
   */
  public KeyStoreSettings trustStore() {
    return trustStorePath != null
        ? new KeyStoreSettings(trustStorePath, trustStorePassword, trustStoreType)
        : KeyStoreSettings.NO_STORE;
  }

  /**
   * Key store key store settings.
   *
   * @return the key store settings
   */
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
        + ", needClientAuth="
        + needClientAuth
        + '}';
  }

  /** The type Builder. */
  public static class Builder {

    private int port;
    private String keyStorePath = getResource(Builder.class, "keystore").toString();
    private String keyStorePassword = "password";
    private String keyManagerPassword = "password";
    private String keyStoreType = "JKS";
    private String trustStorePath = null;
    private String trustStorePassword = "password";
    private String trustStoreType = "JKS";
    private boolean needClientAuth = false;

    /**
     * Port builder.
     *
     * @param port the port
     * @return the builder
     */
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    /**
     * Key store path builder.
     *
     * @param keyStorePath the key store path
     * @return the builder
     */
    public Builder keyStorePath(String keyStorePath) {
      this.keyStorePath = keyStorePath;
      return this;
    }

    /**
     * Key store password builder.
     *
     * @param keyStorePassword the key store password
     * @return the builder
     */
    public Builder keyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
      return this;
    }

    /**
     * Key manager password builder.
     *
     * @param keyStorePassword the key store password
     * @return the builder
     */
    public Builder keyManagerPassword(String keyStorePassword) {
      this.keyManagerPassword = keyStorePassword;
      return this;
    }

    /**
     * Key store type builder.
     *
     * @param keyStoreType the key store type
     * @return the builder
     */
    public Builder keyStoreType(String keyStoreType) {
      this.keyStoreType = keyStoreType;
      return this;
    }

    /**
     * Trust store path builder.
     *
     * @param trustStorePath the trust store path
     * @return the builder
     */
    public Builder trustStorePath(String trustStorePath) {
      this.trustStorePath = trustStorePath;
      return this;
    }

    /**
     * Trust store password builder.
     *
     * @param trustStorePassword the trust store password
     * @return the builder
     */
    public Builder trustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
      return this;
    }

    /**
     * Trust store type builder.
     *
     * @param trustStoreType the trust store type
     * @return the builder
     */
    public Builder trustStoreType(String trustStoreType) {
      this.trustStoreType = trustStoreType;
      return this;
    }

    /**
     * Need client auth builder.
     *
     * @param needClientAuth the need client auth
     * @return the builder
     */
    public Builder needClientAuth(boolean needClientAuth) {
      this.needClientAuth = needClientAuth;
      return this;
    }

    /**
     * Build https settings.
     *
     * @return the https settings
     */
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
          needClientAuth);
    }
  }
}
