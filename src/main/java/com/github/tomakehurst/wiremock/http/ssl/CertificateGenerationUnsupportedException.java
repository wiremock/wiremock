package com.github.tomakehurst.wiremock.http.ssl;

public class CertificateGenerationUnsupportedException extends Exception {
    public CertificateGenerationUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
