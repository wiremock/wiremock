package com.github.tomakehurst.wiremock.crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.Objects.requireNonNull;

public class InMemoryKeyStore {

    public enum KeyStoreType {
        JKS("jks");

        private final String type;

        KeyStoreType(String type) {
            this.type = type;
        }
    }

    private final Secret password;
    private final KeyStore keyStore;

    public InMemoryKeyStore(
        KeyStoreType type,
        Secret password
    ) {
        this.password = requireNonNull(password, "password");
        this.keyStore = initialise(requireNonNull(type, "type"));
    }

    private KeyStore initialise(KeyStoreType type) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type.type);
            keyStore.load(null, password.getValue());
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            return throwUnchecked(e, null);
        }
    }

    public void addPrivateKey(String alias, KeyPair keyPair, CertificateSpecification specification) throws KeyStoreException, CertificateException, InvalidKeyException, SignatureException {
        Certificate cert = specification.certificateFor(keyPair);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), password.getValue(), new Certificate[] { cert });
    }

    public void saveAs(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try {
                keyStore.store(fos, password.getValue());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throwUnchecked(e);
            }
        }
    }
}
