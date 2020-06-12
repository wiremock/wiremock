package com.github.tomakehurst.wiremock.http.ssl;

import org.junit.Test;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CertificateGeneratingX509ExtendedKeyManagerChooseEngineServerAliasDefaultsTest {

    private final X509ExtendedKeyManager keyManagerMock = mock(X509ExtendedKeyManager.class);
    private final SSLEngine sslEngineMock = mock(SSLEngine.class);
    private final SSLEngine nullSslEngine = null;
    private final SSLSession nonExtendedSslSessionMock = mock(SSLSession.class);
    private final ExtendedSSLSession extendedSslSessionMock = mock(ExtendedSSLSession.class);

    private final CertificateGeneratingX509ExtendedKeyManager certificateGeneratingKeyManager = new CertificateGeneratingX509ExtendedKeyManager(
            keyManagerMock,
            mock(DynamicKeyStore.class),
            new SunHostNameMatcher()
    );
    private final Principal[] nullPrincipals = null;

    {
        when(keyManagerMock.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock)).thenReturn("default_alias");
        when(sslEngineMock.getHandshakeSession()).thenReturn(extendedSslSessionMock);
    }

    @Test
    public void returnsNullIfDefaultAliasReturnsNull() {
        given(keyManagerMock.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock)).willReturn(null);

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertNull(alias);
    }

    @Test
    public void returnsDefaultIfEngineIsNull() {
        given(keyManagerMock.chooseEngineServerAlias("RSA", nullPrincipals, nullSslEngine)).willReturn("default_alias");

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, nullSslEngine);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfHandshakeSessionIsNotSupported() {
        given(sslEngineMock.getHandshakeSession()).willThrow(new UnsupportedOperationException());

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfHandshakeSessionIsNotAnAnExtendedSSLSession() {
        given(sslEngineMock.getHandshakeSession()).willReturn(nonExtendedSslSessionMock);

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfGetRequestedServerNamesIsNotSupported() {
        given(extendedSslSessionMock.getRequestedServerNames()).willThrow(new UnsupportedOperationException());

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfThereAreNoSNIServerNames() {
        given(extendedSslSessionMock.getRequestedServerNames()).willReturn(Collections.<SNIServerName>emptyList());

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfThereAreNoSNIHostNames() {
        SNIServerName notAnSIHostName = new SNIServerName(1, new byte[0]) {};
        given(extendedSslSessionMock.getRequestedServerNames()).willReturn(Collections.singletonList(notAnSIHostName));

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    @Test
    public void returnsDefaultIfAnSNIHostNameMatchesTheDefaultCertificate() {
        SNIServerName hostName1 = new SNIHostName("example.com");
        SNIServerName hostName2 = new SNIHostName("wiremock.org");
        SNIServerName hostName3 = new SNIHostName("example.org");
        given(extendedSslSessionMock.getRequestedServerNames()).willReturn(asList(hostName1, hostName2, hostName3));

        X509Certificate matchingCertificate = certificateWithCn("CN=wiremock.org");
        given(keyManagerMock.getCertificateChain("default_alias")).willReturn(new X509Certificate[] { matchingCertificate });

        String alias = certificateGeneratingKeyManager.chooseEngineServerAlias("RSA", nullPrincipals, sslEngineMock);

        assertEquals("default_alias", alias);
    }

    private X509Certificate certificateWithCn(String cn) {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal(cn));
        return certificate;
    }

    public CertificateGeneratingX509ExtendedKeyManagerChooseEngineServerAliasDefaultsTest() throws Exception {}
}
