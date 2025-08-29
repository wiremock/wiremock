/*
 * Copyright (C) 2014-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.ssl;

import com.github.tomakehurst.wiremock.common.Source;
import java.security.KeyStore;

/** The type Key store settings. */
public class KeyStoreSettings {

  /** The constant NO_STORE. */
  public static final KeyStoreSettings NO_STORE = new KeyStoreSettings(null, null, null);

  private final KeyStoreSource keyStoreSource;

  /**
   * Instantiates a new Key store settings.
   *
   * @param keyStoreSource the key store source
   */
  public KeyStoreSettings(KeyStoreSource keyStoreSource) {
    this.keyStoreSource = keyStoreSource;
  }

  /**
   * Instantiates a new Key store settings.
   *
   * @param path the path
   * @param password the password
   * @param type the type
   */
  public KeyStoreSettings(String path, String password, String type) {
    this(
        path != null && password != null && type != null
            ? KeyStoreSourceFactory.getAppropriateForJreVersion(path, type, password.toCharArray())
            : null);
  }

  /**
   * Path string.
   *
   * @return the string
   */
  public String path() {
    if (keyStoreSource instanceof ReadOnlyFileOrClasspathKeyStoreSource) {
      return ((ReadOnlyFileOrClasspathKeyStoreSource) keyStoreSource).getPath();
    }

    return "(no path - custom keystore source)";
  }

  /**
   * Password string.
   *
   * @return the string
   */
  public String password() {
    return keyStoreSource.getKeyStorePassword();
  }

  /**
   * Type string.
   *
   * @return the string
   */
  public String type() {
    return keyStoreSource.getKeyStoreType();
  }

  /**
   * Load store key store.
   *
   * @return the key store
   */
  public KeyStore loadStore() {
    return keyStoreSource.load();
  }

  /**
   * Gets source.
   *
   * @return the source
   */
  public Source<KeyStore> getSource() {
    return keyStoreSource;
  }

  /**
   * Exists boolean.
   *
   * @return the boolean
   */
  public boolean exists() {
    return keyStoreSource.exists();
  }
}
