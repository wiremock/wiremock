package com.github.tomakehurst.wiremock.http.ssl;

import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface X509KeyStore {

    PrivateKey getPrivateKey(String alias);

    X509Certificate[] getCertificateChain(String alias);

    boolean hasCertificateAuthority();

    /**
     * @return the first key & chain that represent a certificate authority
     *         or null if none found
     */
    CertificateAuthority getCertificateAuthority();

    void setKeyEntry(String alias, CertChainAndKey newCertChainAndKey) throws KeyStoreException;
}
