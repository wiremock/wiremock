/*
 * Copyright (C) 2020-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore.KeyStoreType.JKS;
import static com.github.tomakehurst.wiremock.crypto.X509CertificateVersion.V3;
import static java.util.Collections.emptyList;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.DefaultNetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.crypto.CertificateSpecification;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;

public abstract class HttpClientFactoryCertificateVerificationTest {

  protected static final List<String> TRUST_NOBODY = emptyList();

  protected WireMockServer server = null;
  protected CloseableHttpClient client;

  public void startServerAndBuildClient(
      List<String> trustedHosts, String certificateCN, boolean validCertificate) throws Exception {

    InMemoryKeyStore ks = new InMemoryKeyStore(JKS, new Secret("password"));

    KeyPair keyPair = generateKeyPair();

    CertificateSpecification certificateSpecification =
        new X509CertificateSpecification(
            /* version= */ V3,
            /* subject= */ "CN=" + certificateCN,
            /* issuer= */ "CN=wiremock.org",
            /* notBefore= */ new Date(),
            /* notAfter= */ new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)));

    Certificate certificate = certificateSpecification.certificateFor(keyPair);

    ks.addPrivateKey("wiremock", keyPair, certificate);

    File serverKeyStoreFile = File.createTempFile("wiremock-server", "jks");

    ks.saveAs(serverKeyStoreFile);

    server =
        new WireMockServer(
            options()
                .httpDisabled(true)
                .dynamicHttpsPort()
                .keystorePath(serverKeyStoreFile.getAbsolutePath()));
    server.start();

    InMemoryKeyStore clientTrustStore = new InMemoryKeyStore(JKS, new Secret("password"));
    if (validCertificate) {
      clientTrustStore.addCertificate("wiremock", certificate);
    }
    File clientTrustStoreFile = File.createTempFile("wiremock-client", "jks");
    clientTrustStore.saveAs(clientTrustStoreFile);
    KeyStoreSettings clientTrustStoreSettings =
        new KeyStoreSettings(clientTrustStoreFile.getAbsolutePath(), "password", "jks");

    client =
        HttpClientFactory.createClient(
            1000,
            5 * 1000 * 60,
            NO_PROXY,
            clientTrustStoreSettings,
            /* trustSelfSignedCertificates= */ false,
            trustedHosts,
            false,
            DefaultNetworkAddressRules.ALLOW_ALL);
  }

  @AfterEach
  public void stopServer() {
    if (server != null) {
      server.stop();
    }
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    return keyGen.generateKeyPair();
  }
}
