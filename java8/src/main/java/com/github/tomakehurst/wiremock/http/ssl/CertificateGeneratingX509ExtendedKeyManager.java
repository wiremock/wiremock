package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class CertificateGeneratingX509ExtendedKeyManager extends DelegatingX509ExtendedKeyManager {

    private final DynamicKeyStore dynamicKeyStore;
    private final HostNameMatcher hostNameMatcher;

    public CertificateGeneratingX509ExtendedKeyManager(
        X509ExtendedKeyManager keyManager,
        DynamicKeyStore dynamicKeyStore,
        HostNameMatcher hostNameMatcher
    ) {
        super(keyManager);
        this.dynamicKeyStore = requireNonNull(dynamicKeyStore);
        this.hostNameMatcher = requireNonNull(hostNameMatcher);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey original = super.getPrivateKey(alias);
        return original == null ? dynamicKeyStore.getPrivateKey(alias) : original;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] original = super.getCertificateChain(alias);
        if (original == null) {
            return dynamicKeyStore.getCertificateChain(alias);
        } else {
            return original;
        }
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        String defaultAlias = super.chooseServerAlias(keyType, issuers, socket);
        ExtendedSSLSession handshakeSession = getHandshakeSession(socket);
        return tryToChooseServerAlias(keyType, defaultAlias, handshakeSession);
    }

    private static ExtendedSSLSession getHandshakeSession(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession sslSession = getHandshakeSessionIfSupported(sslSocket);
            return getHandshakeSession(sslSession);
        } else {
            return null;
        }
    }

    private static SSLSession getHandshakeSessionIfSupported(SSLSocket sslSocket) {
        try {
            return sslSocket.getHandshakeSession();
        } catch (UnsupportedOperationException e) {
            // TODO log that dynamically generating is not supported
            return null;
        }
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        String defaultAlias = super.chooseEngineServerAlias(keyType, issuers, engine);
        ExtendedSSLSession handshakeSession = getHandshakeSession(engine);
        return tryToChooseServerAlias(keyType, defaultAlias, handshakeSession);
    }

    private static ExtendedSSLSession getHandshakeSession(SSLEngine sslEngine) {
        SSLSession sslSession = getHandshakeSessionIfSupported(sslEngine);
        return getHandshakeSession(sslSession);
    }

    private static SSLSession getHandshakeSessionIfSupported(SSLEngine sslEngine) {
        try {
            return sslEngine.getHandshakeSession();
        } catch (UnsupportedOperationException | NullPointerException e) {
            // TODO log that dynamically generating is not supported
            return null;
        }
    }

    private static ExtendedSSLSession getHandshakeSession(SSLSession handshakeSession) {
        if (handshakeSession instanceof ExtendedSSLSession) {
            return (ExtendedSSLSession) handshakeSession;
        } else {
            return null;
        }
    }

    /**
     * @param keyType non null, may be invalid
     * @param defaultAlias nullable
     * @param handshakeSession nullable
     */
    private String tryToChooseServerAlias(String keyType, String defaultAlias, ExtendedSSLSession handshakeSession) {
        if (defaultAlias != null && handshakeSession != null) {
            return chooseServerAlias(keyType, defaultAlias, handshakeSession);
        } else {
            return defaultAlias;
        }
    }

    /**
     * @param keyType non null, guaranteed to be valid
     * @param defaultAlias non null, guaranteed to match a private key entry
     * @param handshakeSession non null
     */
    private String chooseServerAlias(String keyType, String defaultAlias, ExtendedSSLSession handshakeSession) {
        List<SNIHostName> requestedServerNames = getSNIHostNames(handshakeSession);
        if (requestedServerNames.isEmpty()) {
            return defaultAlias;
        } else {
            return chooseServerAlias(keyType, defaultAlias, requestedServerNames);
        }
    }

    private static List<SNIHostName> getSNIHostNames(ExtendedSSLSession handshakeSession) {
        List<SNIServerName> requestedServerNames = getRequestedServerNames(handshakeSession);
        return requestedServerNames.stream()
                .filter(SNIHostName.class::isInstance)
                .map(SNIHostName.class::cast)
                .collect(Collectors.toList());
    }

    private static List<SNIServerName> getRequestedServerNames(ExtendedSSLSession handshakeSession) {
        try {
            return handshakeSession.getRequestedServerNames();
        } catch (UnsupportedOperationException e) {
            // TODO log that dynamically generating is not supported
            return emptyList();
        }
    }

    /**
     * @param keyType non null, guaranteed to be valid
     * @param defaultAlias non null, guaranteed to match a private key entry
     * @param requestedServerNames non null, non empty
     */
    private String chooseServerAlias(String keyType, String defaultAlias, List<SNIHostName> requestedServerNames) {
        X509Certificate[] certificateChain = super.getCertificateChain(defaultAlias);
        if (certificateChain != null && matches(certificateChain[0], requestedServerNames)) {
            return defaultAlias;
        } else {
            try {
                SNIHostName requestedServerName = requestedServerNames.get(0);
                dynamicKeyStore.generateCertificateIfNecessary(keyType, requestedServerName);
                return requestedServerName.getAsciiName();
            } catch (KeyStoreException | CertificateGenerationUnsupportedException e) {
                // TODO log?
                return defaultAlias;
            }
        }
    }

    private boolean matches(X509Certificate x509Certificate, List<SNIHostName> requestedServerNames) {
        return requestedServerNames.stream().anyMatch(sniHostName -> hostNameMatcher.matches(x509Certificate, sniHostName));
    }
}
