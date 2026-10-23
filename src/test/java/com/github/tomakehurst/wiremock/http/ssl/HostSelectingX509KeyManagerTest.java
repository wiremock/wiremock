/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HostSelectingX509KeyManagerTest {

  private static final char[] PASSWORD = "password".toCharArray();
  private static X509ExtendedKeyManager defaultKeyManager;

  @BeforeAll
  static void createKeyManager() throws Exception {
    // RSA private key entries "client-a" and "client-b"
    KeyStore store = KeyStore.getInstance("PKCS12");
    try (InputStream in =
        Files.newInputStream(Path.of(filePath("multi-client-certs/client-store.p12")))) {
      store.load(in, PASSWORD);
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(store, PASSWORD);
    defaultKeyManager = (X509ExtendedKeyManager) kmf.getKeyManagers()[0];
  }

  private static HostSelectingX509KeyManager keyManager(String... hostAliasPairs) {
    Map<String, String> mapping = new LinkedHashMap<>();
    for (int i = 0; i < hostAliasPairs.length; i += 2) {
      mapping.put(hostAliasPairs[i], hostAliasPairs[i + 1]);
    }
    return new HostSelectingX509KeyManager(defaultKeyManager, mapping);
  }

  @Test
  void matchesExactHostCaseInsensitively() {
    HostSelectingX509KeyManager km = keyManager("API.Example.com", "client-b");

    assertThat(km.configuredAliasFor("api.example.COM"), is("client-b"));
    assertThat(km.configuredAliasFor("other.example.com"), nullValue());
    assertThat(km.configuredAliasFor(null), nullValue());
  }

  @Test
  void wildcardMatchesSubdomainsButNotTheBareDomain() {
    HostSelectingX509KeyManager km = keyManager("*.example.com", "wild");

    assertThat(km.configuredAliasFor("a.example.com"), is("wild"));
    assertThat(km.configuredAliasFor("a.b.example.com"), is("wild"));
    assertThat(km.configuredAliasFor("example.com"), nullValue());
    assertThat(km.configuredAliasFor("badexample.com"), nullValue());
  }

  @Test
  void exactMatchWinsOverWildcardAndLongestWildcardWins() {
    HostSelectingX509KeyManager km =
        keyManager(
            "*.example.com", "wild",
            "*.api.example.com", "client-b",
            "x.api.example.com", "Client-A");

    assertThat(km.configuredAliasFor("x.api.example.com"), is("Client-A"));
    assertThat(km.configuredAliasFor("y.api.example.com"), is("client-b"));
    assertThat(km.configuredAliasFor("y.example.com"), is("wild"));
  }

  @Test
  void choosesTheMappedKeyForTheEnginePeerHost() throws Exception {
    HostSelectingX509KeyManager km = keyManager("mapped.example", "Client-A");

    String alias =
        km.chooseEngineClientAlias(new String[] {"RSA"}, null, engineFor("mapped.example"));

    // PKCS12 reports aliases in lower case; the key manager's own name is returned
    assertThat(alias.toLowerCase(), is("client-a"));
    assertThat(km.getPrivateKey(alias) != null, is(true));
  }

  @Test
  void fallsBackToDefaultSelectionForUnmappedHostsAndUnusableKeyTypes() throws Exception {
    HostSelectingX509KeyManager km = keyManager("mapped.example", "client-b");
    String[] rsa = {"RSA"};

    assertThat(
        km.chooseEngineClientAlias(rsa, null, engineFor("unmapped.example")),
        is(defaultKeyManager.chooseEngineClientAlias(rsa, null, engineFor("unmapped.example"))));
    // client-b is an RSA key, so it cannot satisfy a server that only accepts EC keys
    assertThat(
        km.chooseEngineClientAlias(new String[] {"EC"}, null, engineFor("mapped.example")),
        nullValue());
  }

  private static SSLEngine engineFor(String host) throws Exception {
    return SSLContext.getDefault().createSSLEngine(host, 443);
  }
}
