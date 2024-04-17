/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyStore;
import org.junit.jupiter.api.Test;

 class KeyStoreSettingsTest {

  @Test
   void loadsTrustStoreFromClasspath() {
    KeyStoreSettings trustStoreSettings =
        new KeyStoreSettings(TRUST_STORE_NAME, TRUST_STORE_PASSWORD, "jks");

    KeyStore keyStore = trustStoreSettings.loadStore();
    assertNotNull(keyStore);
  }

  @Test
   void loadsTrustStoreOfTypeJCEKS() {
    KeyStoreSettings trustStoreSettings =
        new KeyStoreSettings(JCEKS_TRUST_STORE_NAME, TRUST_STORE_PASSWORD, "jceks");

    KeyStore keyStore = trustStoreSettings.loadStore();
    assertNotNull(keyStore);
  }

  @Test
   void loadsTrustStoreFromFilesystem() {
    KeyStoreSettings trustStoreSettings =
        new KeyStoreSettings(TRUST_STORE_PATH, TRUST_STORE_PASSWORD, "jks");

    KeyStore keyStore = trustStoreSettings.loadStore();
    assertNotNull(keyStore);
  }

  @Test
   void failsWhenTrustStoreNotFound() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          KeyStoreSettings trustStoreSettings =
              new KeyStoreSettings("test-unknownstore", "", "jks");
          trustStoreSettings.loadStore();
        });
  }
}
