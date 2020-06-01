package com.github.tomakehurst.wiremock.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface CertificateSpecification {
    X509Certificate certificateFor(KeyPair keyPair) throws CertificateException, InvalidKeyException, SignatureException;
}
