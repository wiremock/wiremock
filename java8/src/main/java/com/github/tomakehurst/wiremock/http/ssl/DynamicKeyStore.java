package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SNIHostName;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static java.util.Objects.requireNonNull;

public class DynamicKeyStore {

    private final X509KeyStore keyStore;
    private final CertificateAuthority existingCertificateAuthority;

    public DynamicKeyStore(X509KeyStore keyStore, CertificateAuthority existingCertificateAuthority) {
        this.keyStore = requireNonNull(keyStore);
        this.existingCertificateAuthority = requireNonNull(existingCertificateAuthority);
    }

    PrivateKey getPrivateKey(String alias) {
        return keyStore.getPrivateKey(alias);
    }

    X509Certificate[] getCertificateChain(String alias) {
        return keyStore.getCertificateChain(alias);
    }

    /**
     * @param keyType             non null, guaranteed to be valid
     * @param requestedServerName non null
     */
    void generateCertificateIfNecessary(
        String keyType,
        SNIHostName requestedServerName
    ) throws CertificateGenerationUnsupportedException, KeyStoreException {
        if (getPrivateKey(requestedServerName.getAsciiName()) == null) {
            generateCertificate(keyType, requestedServerName);
        }
    }

    /**
     * @param keyType             non null, guaranteed to be valid
     * @param requestedServerName non null
     */
    private void generateCertificate(
        String keyType,
        SNIHostName requestedServerName
    ) throws CertificateGenerationUnsupportedException, KeyStoreException {
        CertChainAndKey newCertChainAndKey = existingCertificateAuthority.generateCertificate(keyType, requestedServerName);
        keyStore.setKeyEntry(requestedServerName.getAsciiName(), newCertChainAndKey);
    }
}
