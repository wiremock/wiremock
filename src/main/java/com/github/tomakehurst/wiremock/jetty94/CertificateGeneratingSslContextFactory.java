package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.ssl.ApacheHttpHostNameMatcher;
import com.github.tomakehurst.wiremock.http.ssl.CertificateGeneratingX509ExtendedKeyManager;
import com.github.tomakehurst.wiremock.http.ssl.DynamicKeyStore;
import com.github.tomakehurst.wiremock.http.ssl.X509KeyStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;
import java.security.KeyStore;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

class CertificateGeneratingSslContextFactory extends SslContextFactory.Server {

    private final X509KeyStore x509KeyStore;
    private final Notifier notifier;

    CertificateGeneratingSslContextFactory(X509KeyStore x509KeyStore, Notifier notifier) {
        this.x509KeyStore = requireNonNull(x509KeyStore);
        this.notifier = requireNonNull(notifier);
    }

    @Override
    protected KeyManager[] getKeyManagers(KeyStore keyStore) throws Exception {
        KeyManager[] managers = super.getKeyManagers(keyStore);
        return stream(managers).map(manager -> {
            if (manager instanceof X509ExtendedKeyManager) {
                return new CertificateGeneratingX509ExtendedKeyManager(
                        (X509ExtendedKeyManager) manager,
                        new DynamicKeyStore(x509KeyStore),
                        new ApacheHttpHostNameMatcher(),
                        notifier
                );
            } else {
                return manager;
            }
        }).toArray(KeyManager[]::new);
    }
}
