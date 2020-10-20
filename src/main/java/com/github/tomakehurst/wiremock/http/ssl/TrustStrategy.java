package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SSLEngine;
import java.net.Socket;
import java.security.cert.X509Certificate;

public interface TrustStrategy {

    boolean isTrusted(X509Certificate[] chain, String authType);

    boolean isTrusted(X509Certificate[] chain, String authType, Socket socket);

    boolean isTrusted(X509Certificate[] chain, String authType, SSLEngine engine);
}
