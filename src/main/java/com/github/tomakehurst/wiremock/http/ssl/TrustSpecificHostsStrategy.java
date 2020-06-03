package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SSLEngine;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class TrustSpecificHostsStrategy implements TrustStrategy {

    private final List<String> trustedHosts;

    public TrustSpecificHostsStrategy(List<String> trustedHosts) {
        this.trustedHosts = new ArrayList<>(trustedHosts);
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) {
        return false;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType, Socket socket) {
        return trustedHosts.contains(socket.getInetAddress().getHostName());
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        return false;
    }
}
