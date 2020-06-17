package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.http.ssl.CertificateAuthority;
import com.github.tomakehurst.wiremock.http.ssl.CertificateGeneratingX509ExtendedKeyManager;
import com.github.tomakehurst.wiremock.http.ssl.DynamicKeyStore;
import com.github.tomakehurst.wiremock.http.ssl.SunHostNameMatcher;
import com.github.tomakehurst.wiremock.http.ssl.X509KeyStore;
import com.github.tomakehurst.wiremock.jetty9.DefaultMultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509Key;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.util.Arrays.stream;

public class Jetty94HttpServer extends JettyHttpServer {

    public Jetty94HttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }

    @Override
    protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
        return new DefaultMultipartRequestConfigurer();
    }

    @Override
    protected HttpConfiguration createHttpConfig(JettySettings jettySettings) {
        HttpConfiguration httpConfig = super.createHttpConfig(jettySettings);
        httpConfig.setSendXPoweredBy(false);
        httpConfig.setSendServerVersion(false);
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        return httpConfig;
    }

    @Override
    protected ServerConnector createHttpsConnector(Server server, String bindAddress, HttpsSettings httpsSettings, JettySettings jettySettings, NetworkTrafficListener listener) {
        SslContextFactory.Server http2SslContextFactory = buildHttp2SslContextFactory(httpsSettings);

        HttpConfiguration httpConfig = createHttpConfig(jettySettings);

        HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);

        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();

        SslConnectionFactory ssl = new SslConnectionFactory(http2SslContextFactory, alpn.getProtocol());

        ConnectionFactory[] connectionFactories = {
                ssl,
                alpn,
                h2,
                http
        };

        return createServerConnector(
                bindAddress,
                jettySettings,
                httpsSettings.port(),
                listener,
                connectionFactories
        );
    }

    private void setupKeyStore(SslContextFactory.Server sslContextFactory, KeyStoreSettings keyStoreSettings) {
        sslContextFactory.setKeyStorePath(keyStoreSettings.path());
        sslContextFactory.setKeyManagerPassword(keyStoreSettings.password());
        sslContextFactory.setKeyStoreType(keyStoreSettings.type());
    }

    private void setupClientAuth(SslContextFactory.Server sslContextFactory, HttpsSettings httpsSettings) {
        if (httpsSettings.hasTrustStore()) {
            sslContextFactory.setTrustStorePath(httpsSettings.trustStorePath());
            sslContextFactory.setTrustStorePassword(httpsSettings.trustStorePassword());
        }
        sslContextFactory.setNeedClientAuth(httpsSettings.needClientAuth());
    }

    private SslContextFactory.Server buildHttp2SslContextFactory(HttpsSettings httpsSettings) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        setupKeyStore(sslContextFactory, httpsSettings.keyStore());
        setupClientAuth(sslContextFactory, httpsSettings);
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        sslContextFactory.setProvider("Conscrypt");
        return sslContextFactory;
    }

    @Override
    protected HandlerCollection createHandler(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler
    ) {
        HandlerCollection handler = super.createHandler(options, adminRequestHandler, stubRequestHandler);

        if (options.browserProxySettings().enabled()) {
            ManInTheMiddleSslConnectHandler manInTheMiddleSslConnectHandler = new ManInTheMiddleSslConnectHandler(
                    new SslConnectionFactory(
                            buildManInTheMiddleSslContextFactory(options.httpsSettings(), options.browserProxySettings(), options.notifier()),
                    /*
                    If the proxy CONNECT request is made over HTTPS, and the
                    actual content request is made using HTTP/2 tunneled over
                    HTTPS, and an exception is thrown, the server blocks for 30
                    seconds before flushing the response.

                    To fix this, force HTTP/1.1 over TLS when tunneling HTTPS.

                    This also means the HTTP connector does not need the alpn &
                    h2 connection factories as it will not use them.

                    Unfortunately it has proven too hard to write a test to
                    demonstrate the bug; it requires an HTTP client capable of
                    doing ALPN & HTTP/2, which will only offer HTTP/1.1 in the
                    ALPN negotiation when using HTTPS for the initial CONNECT
                    request but will then offer both HTTP/1.1 and HTTP/2 for the
                    actual request (this is how curl 7.64.1 behaves!). Neither
                    Apache HTTP 4, 5, 5 Async, OkHttp, nor the Jetty client
                    could do this. It might be possible to write one using
                    Netty, but it would be hard and time consuming.
                     */
                            HttpVersion.HTTP_1_1.asString()
                    )
            );

            handler.addHandler(manInTheMiddleSslConnectHandler);
        }

        return handler;
    }

    private SslContextFactory.Server buildManInTheMiddleSslContextFactory(HttpsSettings httpsSettings, BrowserProxySettings browserProxySettings, final Notifier notifier) {

        KeyStoreSettings browserProxyCaKeyStore = browserProxySettings.caKeyStore();
        X509KeyStore x509KeyStore = getOrCreateX509KeyStore(browserProxyCaKeyStore);
        CertificateAuthority certificateAuthority = x509KeyStore.getCertificateAuthority();
        if (certificateAuthority == null) {
            throw new IllegalArgumentException("Keystore "+browserProxyCaKeyStore.path()+" does not contain a certificate that can act as a certificate authority");
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server() {
            @Override
            protected KeyManager[] getKeyManagers(KeyStore keyStore) throws Exception {
                KeyManager[] managers = super.getKeyManagers(keyStore);
                return stream(managers).map(manager -> {
                    if (manager instanceof X509ExtendedKeyManager) {
                        return new CertificateGeneratingX509ExtendedKeyManager(
                            (X509ExtendedKeyManager) manager,
                            new DynamicKeyStore(x509KeyStore, certificateAuthority),
                            // TODO write a version of this that doesn't depend on sun internal classes
                            new SunHostNameMatcher(),
                            notifier
                        );
                    } else {
                        return manager;
                    }
                }).toArray(KeyManager[]::new);
            }
        };

        setupKeyStore(sslContextFactory, browserProxyCaKeyStore);
        sslContextFactory.setKeyStorePassword(browserProxyCaKeyStore.password());
        setupClientAuth(sslContextFactory, httpsSettings);
        return sslContextFactory;
    }

    private X509KeyStore getOrCreateX509KeyStore(KeyStoreSettings browserProxyCaKeyStore) {
        if (browserProxyCaKeyStore.exists()) {
            return toX509KeyStore(browserProxyCaKeyStore);
        } else {
            return createX509KeyStoreWithCertificateAuthority(browserProxyCaKeyStore);
        }
    }

    private X509KeyStore toX509KeyStore(KeyStoreSettings browserProxyCaKeyStore) {
        try {
            return new X509KeyStore(browserProxyCaKeyStore.loadStore(), browserProxyCaKeyStore.password().toCharArray());
        } catch (KeyStoreException e) {
            // KeyStore must be loaded here, should never happen
            return throwUnchecked(e, null);
        }
    }

    private X509KeyStore createX509KeyStoreWithCertificateAuthority(KeyStoreSettings browserProxyCaKeyStore) {
        try {
            KeyStore keyStore = KeyStore.getInstance(browserProxyCaKeyStore.type());
            char[] password = browserProxyCaKeyStore.password().toCharArray();
            keyStore.load(null, password);
            CertAndKeyGen newCertAndKey = new CertAndKeyGen("RSA", "SHA256WithRSA");
            newCertAndKey.generate(2048);
            PrivateKey newKey = newCertAndKey.getPrivateKey();

            X509Certificate certificate = newCertAndKey.getSelfCertificate(
                    x500Name("WireMock Local Self Signed Root Certificate"),
                    new Date(),
                    (long) 365 * 24 * 60 * 60 * 10,
                    certificateAuthority(newCertAndKey.getPublicKey())
            );
            keyStore.setKeyEntry("wiremock-ca", newKey, password, new Certificate[] { certificate });

            Path path = Paths.get(browserProxyCaKeyStore.path());
            if (!Files.exists(path.getParent())) {
                Files.createDirectory(path.getParent(), asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE)));
            }
            Path created = Files.createFile(path, asFileAttribute(EnumSet.of(OWNER_READ, OWNER_WRITE)));
            try (FileOutputStream fos = new FileOutputStream(created.toFile())) {
                try {
                    keyStore.store(fos, password);
                } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                    throwUnchecked(e);
                }
            }
            return new X509KeyStore(keyStore, password);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            return throwUnchecked(e, null);
        }
    }

    private static X500Name x500Name(String name){
        try {
            return new X500Name("CN=" + name);
        } catch (IOException e) {
            // X500Name throws IOException for a parse error (which isn't an IO problem...)
            // An SNIHostName should be guaranteed not to have a parse issue
            return throwUnchecked(e, null);
        }
    }

    private static CertificateExtensions certificateAuthority(X509Key publicKey) throws IOException {
        KeyIdentifier keyId = new KeyIdentifier(publicKey);
        byte[] keyIdBytes = keyId.getIdentifier();
        CertificateExtensions extensions = new CertificateExtensions();
        extensions.set(AuthorityKeyIdentifierExtension.NAME, new AuthorityKeyIdentifierExtension(keyId, null, null));
        extensions.set(BasicConstraintsExtension.NAME, new BasicConstraintsExtension(true, Integer.MAX_VALUE));
        KeyUsageExtension keyUsage = new KeyUsageExtension(new boolean[7]);
        keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, true);
        keyUsage.set(KeyUsageExtension.CRL_SIGN, true);
        extensions.set(KeyUsageExtension.NAME, keyUsage);
        extensions.set(SubjectKeyIdentifierExtension.NAME, new SubjectKeyIdentifierExtension(keyIdBytes));
        return extensions;
    }
}
