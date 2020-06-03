/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.github.tomakehurst.wiremock.http.ssl;

import com.github.tomakehurst.wiremock.common.Pair;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.util.Args;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.common.ArrayFunctions.concat;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ListFunctions.splitByType;
import static java.util.Collections.addAll;

/**
 * Builder for {@link SSLContext} instances.
 * <p>
 * Please note: the default Oracle JSSE implementation of {@link SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}
 * accepts multiple key and trust managers, however only only first matching type is ever used.
 * See for example:
 * <a href="http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/SSLContext.html#init%28javax.net.ssl.KeyManager[],%20javax.net.ssl.TrustManager[],%20java.security.SecureRandom%29">
 * SSLContext.html#init
 * </a>
 * <p>
 * TODO Specify which Oracle JSSE versions the above has been verified.
 *  </p>
 * @since 4.4
 */
public class SSLContextBuilder {

    static final String TLS   = "TLS";

    private String protocol;
    private final Set<KeyManager> keyManagers = new LinkedHashSet<>();
    private String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private String keyStoreType = KeyStore.getDefaultType();
    private final Set<TrustManager> trustManagers = new LinkedHashSet<>();
    private String trustManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private SecureRandom secureRandom;
    private Provider provider;

    public static SSLContextBuilder create() {
        return new SSLContextBuilder();
    }

    /**
     * Sets the SSLContext protocol algorithm name.
     *
     * @param protocol
     *            the SSLContext protocol algorithm name of the requested protocol. See
     *            the SSLContext section in the <a href=
     *            "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">Java
     *            Cryptography Architecture Standard Algorithm Name
     *            Documentation</a> for more information.
     * @return this builder
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">Java
     *      Cryptography Architecture Standard Algorithm Name Documentation</a>
     * @since 4.4.7
     */
    public SSLContextBuilder setProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public SSLContextBuilder setSecureRandom(final SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        return this;
    }

    public SSLContextBuilder setProvider(final Provider provider) {
        this.provider = provider;
        return this;
    }

    public SSLContextBuilder setProvider(final String name) {
        this.provider = Security.getProvider(name);
        return this;
    }

    /**
     * Sets the key store type.
     *
     * @param keyStoreType
     *            the SSLkey store type. See
     *            the KeyStore section in the <a href=
     *            "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">Java
     *            Cryptography Architecture Standard Algorithm Name
     *            Documentation</a> for more information.
     * @return this builder
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">Java
     *      Cryptography Architecture Standard Algorithm Name Documentation</a>
     * @since 4.4.7
     */
    public SSLContextBuilder setKeyStoreType(final String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    /**
     * Sets the key manager factory algorithm name.
     *
     * @param keyManagerFactoryAlgorithm
     *            the key manager factory algorithm name of the requested protocol. See
     *            the KeyManagerFactory section in the <a href=
     *            "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyManagerFactory">Java
     *            Cryptography Architecture Standard Algorithm Name
     *            Documentation</a> for more information.
     * @return this builder
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyManagerFactory">Java
     *      Cryptography Architecture Standard Algorithm Name Documentation</a>
     * @since 4.4.7
     */
    public SSLContextBuilder setKeyManagerFactoryAlgorithm(final String keyManagerFactoryAlgorithm) {
        this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
        return this;
    }

    /**
     * Sets the trust manager factory algorithm name.
     *
     * @param trustManagerFactoryAlgorithm
     *            the trust manager algorithm name of the requested protocol. See
     *            the TrustManagerFactory section in the <a href=
     *            "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#TrustManagerFactory">Java
     *            Cryptography Architecture Standard Algorithm Name
     *            Documentation</a> for more information.
     * @return this builder
     * @see <a href=
     *      "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#TrustManagerFactory">Java
     *      Cryptography Architecture Standard Algorithm Name Documentation</a>
     * @since 4.4.7
     */
    public SSLContextBuilder setTrustManagerFactoryAlgorithm(final String trustManagerFactoryAlgorithm) {
        this.trustManagerFactoryAlgorithm = trustManagerFactoryAlgorithm;
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(
        final KeyStore truststore
    ) throws NoSuchAlgorithmException, KeyStoreException {
        return loadTrustMaterial(truststore, null);
    }

    public SSLContextBuilder loadTrustMaterial(
        final KeyStore truststore,
        final TrustStrategy trustStrategy
    ) throws NoSuchAlgorithmException, KeyStoreException {

        String algorithm = trustManagerFactoryAlgorithm == null ? TrustManagerFactory.getDefaultAlgorithm() : trustManagerFactoryAlgorithm;
        TrustManager[] tms = loadTrustManagers(truststore, algorithm);
        TrustManager[] allTms = concat(tms, loadDefaultTrustManagers());

        Pair<List<TrustManager>, List<X509ExtendedTrustManager>> split = splitByType(allTms, X509ExtendedTrustManager.class);
        List<TrustManager> otherTms = split.a;
        List<X509ExtendedTrustManager> x509Tms = split.b;
        if (!x509Tms.isEmpty()) {
            CompositeTrustManager trustManager = new CompositeTrustManager(x509Tms);
            TrustManager tm = trustStrategy == null ? trustManager : addStrategy(trustManager, trustStrategy);
            this.trustManagers.add(tm);
        }
        this.trustManagers.addAll(otherTms);
        return this;
    }

    public SSLContextBuilder loadTrustMaterial(
        final TrustStrategy trustStrategy
    ) {

        TrustManager[] tms = loadDefaultTrustManagers();
        TrustManager[] tmsWithStrategy = addStrategy(tms, trustStrategy);

        addAll(this.trustManagers, tmsWithStrategy);
        return this;
    }

    private TrustManager[] loadTrustManagers(KeyStore truststore, String algorithm) throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(algorithm);
        tmfactory.init(truststore);
        TrustManager[] tms = tmfactory.getTrustManagers();
        return tms == null ? new TrustManager[0] : tms;
    }

    private TrustManager[] loadDefaultTrustManagers() {
        try {
            return loadTrustManagers(null, TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            return throwUnchecked(e, null);
        }
    }

    private TrustManager[] addStrategy(TrustManager[] allTms, TrustStrategy trustStrategy) {
        TrustManager[] withStrategy = new TrustManager[allTms.length];
        for (int i = 0; i < allTms.length; i++) {
            withStrategy[i] = addStrategy(allTms[i], trustStrategy);
        }
        return withStrategy;
    }

    private TrustManager addStrategy(TrustManager tm, TrustStrategy trustStrategy) {
        if (tm instanceof X509ExtendedTrustManager) {
            return new TrustManagerDelegate((X509ExtendedTrustManager) tm, trustStrategy);
        } else {
            return tm;
        }
    }

    public SSLContextBuilder loadKeyMaterial(
            final KeyStore keystore,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        final KeyManagerFactory kmfactory = KeyManagerFactory
                .getInstance(keyManagerFactoryAlgorithm == null ? KeyManagerFactory.getDefaultAlgorithm()
                        : keyManagerFactoryAlgorithm);
        kmfactory.init(keystore, keyPassword);
        final KeyManager[] kms = kmfactory.getKeyManagers();
        if (kms != null) {
            if (aliasStrategy != null) {
                for (int i = 0; i < kms.length; i++) {
                    final KeyManager km = kms[i];
                    if (km instanceof X509ExtendedKeyManager) {
                        kms[i] = new KeyManagerDelegate((X509ExtendedKeyManager) km, aliasStrategy);
                    }
                }
            }
            addAll(keyManagers, kms);
        }
        return this;
    }

    public SSLContextBuilder loadKeyMaterial(
            final KeyStore keystore,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return loadKeyMaterial(keystore, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(
            final File file,
            final char[] storePassword,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(file, "Keystore file");
        final KeyStore identityStore = KeyStore.getInstance(keyStoreType);
        try (FileInputStream inStream = new FileInputStream(file)) {
            identityStore.load(inStream, storePassword);
        }
        return loadKeyMaterial(identityStore, keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(
            final File file,
            final char[] storePassword,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(file, storePassword, keyPassword, null);
    }

    public SSLContextBuilder loadKeyMaterial(
            final URL url,
            final char[] storePassword,
            final char[] keyPassword,
            final PrivateKeyStrategy aliasStrategy) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        Args.notNull(url, "Keystore URL");
        final KeyStore identityStore = KeyStore.getInstance(keyStoreType);
        try (InputStream inStream = url.openStream()) {
            identityStore.load(inStream, storePassword);
        }
        return loadKeyMaterial(identityStore, keyPassword, aliasStrategy);
    }

    public SSLContextBuilder loadKeyMaterial(
            final URL url,
            final char[] storePassword,
            final char[] keyPassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateException, IOException {
        return loadKeyMaterial(url, storePassword, keyPassword, null);
    }

    protected void initSSLContext(
            final SSLContext sslContext,
            final Collection<KeyManager> keyManagers,
            final Collection<TrustManager> trustManagers,
            final SecureRandom secureRandom) throws KeyManagementException {
        sslContext.init(
                !keyManagers.isEmpty() ? keyManagers.toArray(new KeyManager[0]) : null,
                !trustManagers.isEmpty() ? trustManagers.toArray(new TrustManager[0]) : null,
                secureRandom);
    }

    public SSLContext build() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext;
        final String protocolStr = this.protocol != null ? this.protocol : TLS;
        if (this.provider != null) {
            sslContext = SSLContext.getInstance(protocolStr, this.provider);
        } else {
            sslContext = SSLContext.getInstance(protocolStr);
        }
        initSSLContext(sslContext, keyManagers, trustManagers, secureRandom);
        return sslContext;
    }

    static class TrustManagerDelegate extends X509ExtendedTrustManager {

        private final X509ExtendedTrustManager trustManager;
        private final TrustStrategy trustStrategy;

        TrustManagerDelegate(final X509ExtendedTrustManager trustManager, final TrustStrategy trustStrategy) {
            this.trustManager = trustManager;
            this.trustStrategy = trustStrategy;
        }

        @Override
        public void checkClientTrusted(
                final X509Certificate[] chain, final String authType) throws CertificateException {
            this.trustManager.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(
                final X509Certificate[] chain, final String authType) throws CertificateException {
            if (!this.trustStrategy.isTrusted(chain, authType)) {
                this.trustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.trustManager.getAcceptedIssuers();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            trustManager.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            if (!this.trustStrategy.isTrusted(chain, authType, socket)) {
                trustManager.checkServerTrusted(chain, authType, socket);
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            trustManager.checkClientTrusted(chain, authType, engine);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            if (!this.trustStrategy.isTrusted(chain, authType, engine)) {
                trustManager.checkServerTrusted(chain, authType, engine);
            }
        }
    }

    static class KeyManagerDelegate extends X509ExtendedKeyManager {

        private final X509ExtendedKeyManager keyManager;
        private final PrivateKeyStrategy aliasStrategy;

        KeyManagerDelegate(final X509ExtendedKeyManager keyManager, final PrivateKeyStrategy aliasStrategy) {
            super();
            this.keyManager = keyManager;
            this.aliasStrategy = aliasStrategy;
        }

        @Override
        public String[] getClientAliases(
                final String keyType, final Principal[] issuers) {
            return this.keyManager.getClientAliases(keyType, issuers);
        }

        public Map<String, PrivateKeyDetails> getClientAliasMap(
                final String[] keyTypes, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<>();
            for (final String keyType: keyTypes) {
                final String[] aliases = this.keyManager.getClientAliases(keyType, issuers);
                if (aliases != null) {
                    for (final String alias: aliases) {
                        validAliases.put(alias,
                                new PrivateKeyDetails(keyType, this.keyManager.getCertificateChain(alias)));
                    }
                }
            }
            return validAliases;
        }

        public Map<String, PrivateKeyDetails> getServerAliasMap(
                final String keyType, final Principal[] issuers) {
            final Map<String, PrivateKeyDetails> validAliases = new HashMap<>();
            final String[] aliases = this.keyManager.getServerAliases(keyType, issuers);
            if (aliases != null) {
                for (final String alias: aliases) {
                    validAliases.put(alias,
                            new PrivateKeyDetails(keyType, this.keyManager.getCertificateChain(alias)));
                }
            }
            return validAliases;
        }

        @Override
        public String chooseClientAlias(
                final String[] keyTypes, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket);
        }

        @Override
        public String[] getServerAliases(
                final String keyType, final Principal[] issuers) {
            return this.keyManager.getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(
                final String keyType, final Principal[] issuers, final Socket socket) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, socket);
        }

        @Override
        public X509Certificate[] getCertificateChain(final String alias) {
            return this.keyManager.getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(final String alias) {
            return this.keyManager.getPrivateKey(alias);
        }

        @Override
        public String chooseEngineClientAlias(
                final String[] keyTypes, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getClientAliasMap(keyTypes, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, null);
        }

        @Override
        public String chooseEngineServerAlias(
                final String keyType, final Principal[] issuers, final SSLEngine sslEngine) {
            final Map<String, PrivateKeyDetails> validAliases = getServerAliasMap(keyType, issuers);
            return this.aliasStrategy.chooseAlias(validAliases, null);
        }

    }

    @Override
    public String toString() {
        return "[provider=" + provider + ", protocol=" + protocol + ", keyStoreType=" + keyStoreType
                + ", keyManagerFactoryAlgorithm=" + keyManagerFactoryAlgorithm + ", keyManagers=" + keyManagers
                + ", trustManagerFactoryAlgorithm=" + trustManagerFactoryAlgorithm + ", trustManagers=" + trustManagers
                + ", secureRandom=" + secureRandom + "]";
    }

}
