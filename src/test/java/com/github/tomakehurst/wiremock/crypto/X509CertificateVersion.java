package com.github.tomakehurst.wiremock.crypto;

import sun.security.x509.CertificateVersion;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

@SuppressWarnings("sunapi")
public enum X509CertificateVersion {

    V1(CertificateVersion.V1),
    V2(CertificateVersion.V2),
    V3(CertificateVersion.V3);

    private final CertificateVersion version;

    X509CertificateVersion(int version) {
        this.version = getVersion(version);
    }

    private static CertificateVersion getVersion(int version) {
        try {
            return new CertificateVersion(version);
        } catch (IOException e) {
            return throwUnchecked(e, null);
        }
    }

    CertificateVersion getVersion() {
        return version;
    }
}
