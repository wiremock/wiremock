package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SNIHostName;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.util.Objects.requireNonNull;

class DynamicKeyStore implements X509KeyStore {

    private final X509KeyStore keyStore;
    private final CertificateAuthority existingCertificateAuthority;

    DynamicKeyStore(X509KeyStore keyStore) {
        this.keyStore = requireNonNull(keyStore);
        this.existingCertificateAuthority = keyStore.getCertificateAuthority();
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return keyStore.getPrivateKey(alias);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return keyStore.getCertificateChain(alias);
    }

    /**
     * @param keyType             non null, guaranteed to be valid
     * @param requestedServerName non null
     */
    void generateCertificateIfNecessary(
        String keyType,
        SNIHostName requestedServerName
    ) throws CertificateException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, KeyStoreException {
        if (getPrivateKey(requestedServerName.getAsciiName()) == null) {
            generateCertificate(keyType, requestedServerName);
        }
    }

    /**
     * @param keyType             non null, guaranteed to be valid
     * @param requestedServerName non null
     */
    void generateCertificate(
        String keyType,
        SNIHostName requestedServerName
    ) throws CertificateException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException, KeyStoreException {
        CertChainAndKey newCertChainAndKey = existingCertificateAuthority.generateCertificate(keyType, requestedServerName);
        setKeyEntry(requestedServerName.getAsciiName(), newCertChainAndKey);
    }

    @Override
    public boolean hasCertificateAuthority() {
        return existingCertificateAuthority != null;
    }

    @Override
    public CertificateAuthority getCertificateAuthority() {
        return keyStore.getCertificateAuthority();
    }

    @Override
    public void setKeyEntry(String alias, CertChainAndKey newCertChainAndKey) throws KeyStoreException {
        keyStore.setKeyEntry(alias, newCertChainAndKey);
    }
}
