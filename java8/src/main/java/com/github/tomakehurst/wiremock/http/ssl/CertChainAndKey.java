package com.github.tomakehurst.wiremock.http.ssl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

class CertChainAndKey {
    final X509Certificate[] certificateChain;
    final PrivateKey key;

    CertChainAndKey(X509Certificate[] certificateChain, PrivateKey key) {
        this.certificateChain = certificateChain;
        this.key = key;
    }
}
