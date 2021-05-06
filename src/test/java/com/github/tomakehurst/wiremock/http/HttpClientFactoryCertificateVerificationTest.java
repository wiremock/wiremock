package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.crypto.CertificateSpecification;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import org.apache.http.client.HttpClient;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore.KeyStoreType.JKS;
import static com.github.tomakehurst.wiremock.crypto.X509CertificateVersion.V3;
import static java.util.Collections.emptyList;

public abstract class HttpClientFactoryCertificateVerificationTest {

    protected static final List<String> TRUST_NOBODY = emptyList();

    private final List<String> trustedHosts;
    private final String certificateCN;
    private final boolean validCertificate;
    protected WireMockServer server = null;
    protected HttpClient client;

    protected HttpClientFactoryCertificateVerificationTest(
        List<String> trustedHosts,
        String certificateCN,
        boolean validCertificate
    ) {
        this.trustedHosts = trustedHosts;
        this.certificateCN = certificateCN;
        this.validCertificate = validCertificate;
    }

    @Before
    public void startServerAndBuildClient() throws Exception {

        InMemoryKeyStore ks = new InMemoryKeyStore(JKS, new Secret("password"));

        KeyPair keyPair = generateKeyPair();

        CertificateSpecification certificateSpecification = new X509CertificateSpecification(
                /* version = */V3,
                /* subject = */"CN=" + certificateCN,
                /* issuer = */"CN=wiremock.org",
                /* notBefore = */new Date(),
                /* notAfter = */new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000))
        );

        Certificate certificate = certificateSpecification.certificateFor(keyPair);

        ks.addPrivateKey("wiremock", keyPair, certificate);

        File serverKeyStoreFile = File.createTempFile("wiremock-server", "jks");

        ks.saveAs(serverKeyStoreFile);

        server = new WireMockServer(options()
                .httpDisabled(true)
                .dynamicHttpsPort()
                .keystorePath(serverKeyStoreFile.getAbsolutePath())
        );
        server.start();


        InMemoryKeyStore clientTrustStore = new InMemoryKeyStore(JKS, new Secret("password"));
        if (validCertificate) {
            clientTrustStore.addCertificate("wiremock", certificate);
        }
        File clientTrustStoreFile = File.createTempFile("wiremock-client", "jks");
        clientTrustStore.saveAs(clientTrustStoreFile);
        KeyStoreSettings clientTrustStoreSettings = new KeyStoreSettings(clientTrustStoreFile.getAbsolutePath(), "password", "jks");

        client = HttpClientFactory.createClient(
                1000,
                5 * 1000 * 60,
                NO_PROXY,
                clientTrustStoreSettings,
                /* trustSelfSignedCertificates = */false,
                trustedHosts,
                false
        );
    }

    @After
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
