package com.github.tomakehurst.wiremock.http.ssl;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLException;
import java.security.cert.X509Certificate;

public class ApacheHttpHostNameMatcher implements HostNameMatcher {
    @Override
    public Boolean matches(X509Certificate x509Certificate, SNIHostName sniHostName) {
        try {
            new DefaultHostnameVerifier().verify(sniHostName.getAsciiName(), x509Certificate);
            return true;
        } catch (SSLException e) {
            return false;
        }
    }
}
