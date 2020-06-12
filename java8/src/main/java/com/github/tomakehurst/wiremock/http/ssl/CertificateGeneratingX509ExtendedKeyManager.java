package com.github.tomakehurst.wiremock.http.ssl;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.util.HostnameChecker;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.DNSName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.emptyList;

@SuppressWarnings("sunapi")
public class CertificateGeneratingX509ExtendedKeyManager extends DelegatingX509ExtendedKeyManager {

    private final KeyStore keyStore;
    private final char[] password;
    private final CertChainAndKey existingCertificateAuthority;

    public CertificateGeneratingX509ExtendedKeyManager(X509ExtendedKeyManager keyManager, KeyStore keyStore, char[] keyPassword) {
        super(keyManager);
        this.keyStore = keyStore;
        this.password = keyPassword;
        existingCertificateAuthority = findExistingCertificateAuthority();
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey original = super.getPrivateKey(alias);
        return original == null ? getDynamicPrivateKey(alias) : original;
    }

    private PrivateKey getDynamicPrivateKey(String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            return null;
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] original = super.getCertificateChain(alias);
        if (original == null) {
            return getDynamicCertificateChain(alias);
        } else {
            return original;
        }
    }

    private X509Certificate[] getDynamicCertificateChain(String alias) {
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
        if (defaultAlias != null && handshakeSession != null && existingCertificateAuthority != null) {
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
        List<SNIHostName> requestedHostNames = new ArrayList<>(requestedServerNames.size());
        for (SNIServerName serverName: requestedServerNames) {
            if (serverName instanceof SNIHostName) {
                requestedHostNames.add((SNIHostName) serverName);
            }
        }
        return requestedHostNames;
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
        X509Certificate[] certificateChain = getCertificateChain(defaultAlias);
        if (matches(certificateChain[0], requestedServerNames)) {
            return defaultAlias;
        } else {
            try {
                return generateCertificate(keyType, requestedServerNames.get(0));
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | IOException | SignatureException e) {
                // TODO log?
                return defaultAlias;
            }
        }
    }

    /**
     * @param keyType non null, guaranteed to be valid
     * @param requestedServerName non null
     * @return an alias to a new private key & certificate for the first requested server name
     */
    private String generateCertificate(
        String keyType,
        SNIHostName requestedServerName
    ) throws CertificateException, NoSuchAlgorithmException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, KeyStoreException {
        String requestedNameString = requestedServerName.getAsciiName();

        CertChainAndKey newCertChainAndKey = generateCertificate(keyType, requestedNameString);

        keyStore.setKeyEntry(requestedNameString, newCertChainAndKey.key, password, newCertChainAndKey.certificateChain);
        return requestedNameString;
    }

    private CertChainAndKey generateCertificate(
        String keyType,
        String requestedNameString
    ) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, CertificateException, SignatureException, IOException {
        CertAndKeyGen newCertAndKey = new CertAndKeyGen(keyType, "SHA256With"+keyType, null);
        newCertAndKey.generate(2048);
        PrivateKey newKey = newCertAndKey.getPrivateKey();

        X509Certificate certificate = newCertAndKey.getSelfCertificate(
                new X500Name("CN=" + requestedNameString),
                new Date(),
                (long) 365 * 24 * 60 * 60,
                subjectAlternativeName(requestedNameString)
        );

        X509Certificate[] signingChain = existingCertificateAuthority.certificateChain;
        PrivateKey signingKey = existingCertificateAuthority.key;

        X509Certificate signed = createSignedCertificate(certificate, signingChain[0], signingKey);
        X509Certificate[] fullChain = new X509Certificate[signingChain.length + 1];
        fullChain[0] = signed;
        System.arraycopy(signingChain, 0, fullChain, 1, signingChain.length);
        return new CertChainAndKey(fullChain, newKey);
    }

    private static CertificateExtensions subjectAlternativeName(String requestedNameString) throws IOException {
        GeneralName name = new GeneralName(new DNSName(requestedNameString));
        GeneralNames names = new GeneralNames();
        names.add(name);
        SubjectAlternativeNameExtension subjectAlternativeNameExtension = new SubjectAlternativeNameExtension(names);

        CertificateExtensions extensions = new CertificateExtensions();
        extensions.set(SubjectAlternativeNameExtension.NAME, subjectAlternativeNameExtension);
        return extensions;
    }

    private CertChainAndKey findExistingCertificateAuthority() {
        Enumeration<String> aliases;
        try {
            aliases = keyStore.aliases();
        } catch (KeyStoreException e) {
            aliases = Collections.emptyEnumeration();
        }
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            CertChainAndKey key = getCertChainAndKey(alias);
            if (key != null) return key;
        }
        return null;
    }

    private CertChainAndKey getCertChainAndKey(String alias) {
        X509Certificate[] chain = getCertificateChain(alias);
        PrivateKey key = getPrivateKey(alias);
        if (isCertificateAuthority(chain[0]) && key != null) {
            return new CertChainAndKey(chain, key);
        } else {
            return null;
        }
    }

    private static boolean isCertificateAuthority(X509Certificate certificate) {
        boolean[] keyUsage = certificate.getKeyUsage();
        return keyUsage != null && keyUsage.length > 5 && keyUsage[5];
    }

    private static X509Certificate createSignedCertificate(X509Certificate certificate, X509Certificate issuerCertificate, PrivateKey issuerPrivateKey) {
        try {
            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = certificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            info.set(X509CertInfo.ISSUER, issuer);

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);

            return outCert;
        } catch (Exception ex) {
            // TODO log failure to generate certificate
        }
        return null;
    }

    private static boolean matches(X509Certificate x509Certificate, List<SNIHostName> requestedServerNames) {
        for (SNIHostName serverName : requestedServerNames) {
            if (matches(x509Certificate, serverName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(X509Certificate x509Certificate, SNIHostName hostName) {
        try {
            HostnameChecker instance = HostnameChecker.getInstance(HostnameChecker.TYPE_TLS);
            instance.match(hostName.getAsciiName(), x509Certificate);
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }
}
