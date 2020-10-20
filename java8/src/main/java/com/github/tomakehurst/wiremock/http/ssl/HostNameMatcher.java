package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SNIHostName;
import java.security.cert.X509Certificate;

@FunctionalInterface
public interface HostNameMatcher {
    Boolean matches(X509Certificate x509Certificate, SNIHostName sniHostName);
}
