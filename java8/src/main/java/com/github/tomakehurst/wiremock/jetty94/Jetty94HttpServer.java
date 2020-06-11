package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty9.DefaultMultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;

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

    private SslContextFactory.Server buildBasicSslContextFactory(HttpsSettings httpsSettings) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePath(httpsSettings.keyStorePath());
        sslContextFactory.setKeyManagerPassword(httpsSettings.keyStorePassword());
        sslContextFactory.setKeyStoreType(httpsSettings.keyStoreType());
        if (httpsSettings.hasTrustStore()) {
            sslContextFactory.setTrustStorePath(httpsSettings.trustStorePath());
            sslContextFactory.setTrustStorePassword(httpsSettings.trustStorePassword());
        }
        sslContextFactory.setNeedClientAuth(httpsSettings.needClientAuth());
        return sslContextFactory;
    }


    private SslContextFactory.Server buildHttp2SslContextFactory(HttpsSettings httpsSettings) {
        SslContextFactory.Server sslContextFactory = buildBasicSslContextFactory(httpsSettings);
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

        ManInTheMiddleSslConnectHandler manInTheMiddleSslConnectHandler = new ManInTheMiddleSslConnectHandler(
                new SslConnectionFactory(
                    buildBasicSslContextFactory(options.httpsSettings()),
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

        return handler;
    }
}
