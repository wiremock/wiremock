package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SNIHostName;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;

import static java.util.Objects.requireNonNull;

class DynamicKeyStore {

    private final KeyStore keyStore;
    private final char[] password;
    private final CertificateAuthority existingCertificateAuthority;

    DynamicKeyStore(KeyStore keyStore, char[] password) {
        this.keyStore = requireNonNull(keyStore);
        this.password = requireNonNull(password);
        this.existingCertificateAuthority = findExistingCertificateAuthority();
    }

    PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            return null;
        }
    }

    X509Certificate[] getCertificateChain(String alias) {
        try {
            Certificate[] fromKeyStore = keyStore.getCertificateChain(alias);
            if (fromKeyStore != null && areX509Certificates(fromKeyStore)) {
                return convertToX509(fromKeyStore);
            } else {
                return null;
            }
        } catch (KeyStoreException e) {
            return null;
        }
    }

    private static boolean areX509Certificates(Certificate[] fromKeyStore) {
        return fromKeyStore.length == 0 || fromKeyStore[0] instanceof X509Certificate;
    }

    private static X509Certificate[] convertToX509(Certificate[] fromKeyStore) {
        X509Certificate[] result = new X509Certificate[fromKeyStore.length];
        for (int i = 0; i < fromKeyStore.length; i++) {
            result[i] = (X509Certificate) fromKeyStore[i];
        }
        return result;
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
        keyStore.setKeyEntry(requestedServerName.getAsciiName(), newCertChainAndKey.key, password, newCertChainAndKey.certificateChain);
    }

    boolean hasCertificateAuthority() {
        return existingCertificateAuthority != null;
    }

    private CertificateAuthority findExistingCertificateAuthority() {
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            aliases = Collections.emptyEnumeration();
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            CertificateAuthority key = getCertChainAndKey(alias);
            if (key != null) return key;
        }
        return null;
    }

    private CertificateAuthority getCertChainAndKey(String alias) {
        X509Certificate[] chain = getCertificateChain(alias);
        PrivateKey key = getPrivateKey(alias);
        if (isCertificateAuthority(chain[0]) && key != null) {
            return new CertificateAuthority(chain, key);
        } else {
            return null;
        }
    }

    private static boolean isCertificateAuthority(X509Certificate certificate) {
        boolean[] keyUsage = certificate.getKeyUsage();
        return keyUsage != null && keyUsage.length > 5 && keyUsage[5];
    }
}
