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
package com.github.tomakehurst.wiremock.common;

import static java.util.Collections.emptyList;

import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import java.io.File;
import java.util.List;
import java.util.Objects;

/** The type Browser proxy settings. */
public final class BrowserProxySettings {

  /** The constant DEFAULT_CA_KEYSTORE_PATH. */
  public static final String DEFAULT_CA_KEYSTORE_PATH =
      new File(
              System.getProperty("user.home")
                  + File.separatorChar
                  + ".wiremock"
                  + File.separatorChar
                  + "ca-keystore.jks"
                  + File.separatorChar)
          .getAbsolutePath();

  /** The constant DEFAULT_CA_KESTORE_PASSWORD. */
  public static final String DEFAULT_CA_KESTORE_PASSWORD = "password";

  /** The constant DISABLED. */
  public static BrowserProxySettings DISABLED = new Builder().build();

  private final boolean enabled;
  private final boolean trustAllProxyTargets;
  private final List<String> trustedProxyTargets;
  private final KeyStoreSettings caKeyStoreSettings;

  /**
   * Instantiates a new Browser proxy settings.
   *
   * @param enabled the enabled
   * @param trustAllProxyTargets the trust all proxy targets
   * @param trustedProxyTargets the trusted proxy targets
   * @param caKeyStoreSettings the ca key store settings
   */
  public BrowserProxySettings(
      boolean enabled,
      boolean trustAllProxyTargets,
      List<String> trustedProxyTargets,
      KeyStoreSettings caKeyStoreSettings) {
    this.enabled = enabled;
    this.trustAllProxyTargets = trustAllProxyTargets;
    this.trustedProxyTargets = trustedProxyTargets;
    this.caKeyStoreSettings = caKeyStoreSettings;
  }

  /**
   * Enabled boolean.
   *
   * @return the boolean
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Trust all proxy targets boolean.
   *
   * @return the boolean
   */
  public boolean trustAllProxyTargets() {
    return trustAllProxyTargets;
  }

  /**
   * Trusted proxy targets list.
   *
   * @return the list
   */
  public List<String> trustedProxyTargets() {
    return trustedProxyTargets;
  }

  /**
   * Ca key store key store settings.
   *
   * @return the key store settings
   */
  public KeyStoreSettings caKeyStore() {
    return caKeyStoreSettings;
  }

  @Override
  public String toString() {
    return "BrowserProxySettings{"
        + "enabled="
        + enabled
        + ", trustAllProxyTargets="
        + trustAllProxyTargets
        + ", trustedProxyTargets="
        + trustedProxyTargets
        + ", caKeyStore='"
        + caKeyStoreSettings.path()
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BrowserProxySettings that = (BrowserProxySettings) o;
    return enabled == that.enabled
        && trustAllProxyTargets == that.trustAllProxyTargets
        && Objects.equals(trustedProxyTargets, that.trustedProxyTargets)
        && Objects.equals(caKeyStoreSettings, that.caKeyStoreSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStoreSettings);
  }

  /** The type Builder. */
  public static final class Builder {

    private boolean enabled = false;
    private boolean trustAllProxyTargets = false;
    private List<String> trustedProxyTargets = emptyList();

    private KeyStoreSettings caKeyStoreSettings = KeyStoreSettings.NO_STORE;

    /**
     * Enabled builder.
     *
     * @param enabled the enabled
     * @return the builder
     */
    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /**
     * Trust all proxy targets builder.
     *
     * @param trustAllProxyTargets the trust all proxy targets
     * @return the builder
     */
    public Builder trustAllProxyTargets(boolean trustAllProxyTargets) {
      this.trustAllProxyTargets = trustAllProxyTargets;
      return this;
    }

    /**
     * Trusted proxy targets builder.
     *
     * @param trustedProxyTargets the trusted proxy targets
     * @return the builder
     */
    public Builder trustedProxyTargets(List<String> trustedProxyTargets) {
      this.trustedProxyTargets = trustedProxyTargets;
      return this;
    }

    /**
     * Ca key store settings builder.
     *
     * @param caKeyStoreSettings the ca key store settings
     * @return the builder
     */
    public Builder caKeyStoreSettings(KeyStoreSettings caKeyStoreSettings) {
      this.caKeyStoreSettings = caKeyStoreSettings;
      return this;
    }

    /**
     * Build browser proxy settings.
     *
     * @return the browser proxy settings
     */
    public BrowserProxySettings build() {
      return new BrowserProxySettings(
          enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStoreSettings);
    }
  }
}
