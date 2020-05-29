package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.crypto.X509CertificateVersion.V3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ProxyResponseRendererTest {

    @Rule
    public WireMockRule origin = new WireMockRule(options()
            .httpDisabled(true)
            .dynamicHttpsPort()
            .keystorePath(generateKeystore().getAbsolutePath())
    );

    private final ProxyResponseRenderer proxyResponseRenderer = buildProxyResponseRenderer(false);

    @Test
    public void acceptsAnyCertificateForStandardProxying() {

        origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

        ServeEvent serveEvent = reverseProxyServeEvent("/proxied");

        Response response = proxyResponseRenderer.render(serveEvent);

        assertEquals(response.getBodyAsString(), "Result");
    }

    @Test
    public void rejectsSelfSignedCertificateForReverseProxying() {

        origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

        final ServeEvent serveEvent = forwardProxyServeEvent("/proxied");

        SSLHandshakeException e = assertThrows(SSLHandshakeException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                proxyResponseRenderer.render(serveEvent);
            }
        });

        assertThat(e.getMessage(), containsString("unable to find valid certification path to requested target"));
    }

    @Test
    public void acceptsSelfSignedCertificateForReverseProxyingIfTrustAll() {

        final ProxyResponseRenderer trustAllProxyResponseRenderer = buildProxyResponseRenderer(true);

        origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

        final ServeEvent serveEvent = forwardProxyServeEvent("/proxied");

        Response response = trustAllProxyResponseRenderer.render(serveEvent);

        assertEquals(response.getBodyAsString(), "Result");
    }

    private ServeEvent reverseProxyServeEvent(String path) {
        return serveEvent(path, false);
    }

    private ServeEvent forwardProxyServeEvent(String path) {
        return serveEvent(path, true);
    }

    private ServeEvent serveEvent(String path, boolean isBrowserProxyRequest) {
        LoggedRequest loggedRequest = new LoggedRequest(
                /* url = */path,
                /* absoluteUrl = */origin.url(path),
                /* method = */ RequestMethod.GET,
                /* clientIp = */"127.0.0.1",
                /* headers = */new HttpHeaders(),
                /* cookies = */new HashMap<String, Cookie>(),
                /* isBrowserProxyRequest = */isBrowserProxyRequest,
                /* loggedDate = */new Date(),
                /* body = */new byte[0],
                /* multiparts = */null
        );
        ResponseDefinition responseDefinition = aResponse().proxiedFrom(origin.baseUrl()).build();
        responseDefinition.setOriginalRequest(loggedRequest);

        return ServeEvent.of(
                loggedRequest,
                responseDefinition,
                new StubMapping()
        );
    }

    private File generateKeystore() throws Exception {

        InMemoryKeyStore ks = new InMemoryKeyStore(InMemoryKeyStore.KeyStoreType.JKS, new Secret("password"));

        ks.addPrivateKey("wiremock", generateKeyPair(), new X509CertificateSpecification(
                /* version = */V3,
                /* subject = */"CN=wiremock.org",
                /* issuer = */"CN=wiremock.org",
                /* notBefore = */new Date(),
                /* notAfter = */new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000))
        ));

        File keystoreFile = File.createTempFile("wiremock-test", "keystore");

        ks.saveAs(keystoreFile);

        return keystoreFile;
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

    private ProxyResponseRenderer buildProxyResponseRenderer(boolean trustAll) {
        return new ProxyResponseRenderer(
                ProxySettings.NO_PROXY,
                KeyStoreSettings.NO_STORE,
                /* preserveHostHeader = */ false,
                /* hostHeaderValue = */ null,
                new GlobalSettingsHolder(),
                trustAll
        );
    }

    // Just exists to make the compiler happy by having the throws clause
    public ProxyResponseRendererTest() throws Exception {}
}
