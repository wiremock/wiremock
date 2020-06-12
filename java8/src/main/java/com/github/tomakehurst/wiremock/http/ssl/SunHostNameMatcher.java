package com.github.tomakehurst.wiremock.http.ssl;

import sun.security.util.HostnameChecker;

import javax.net.ssl.SNIHostName;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static sun.security.util.HostnameChecker.TYPE_TLS;

@SuppressWarnings("sunapi")
public class SunHostNameMatcher implements HostNameMatcher {
    @Override
    public Boolean matches(X509Certificate x509Certificate, SNIHostName sniHostName) {
        try {
            HostnameChecker instance = HostnameChecker.getInstance(TYPE_TLS);
            instance.match(sniHostName.getAsciiName(), x509Certificate);
            return true;
        } catch (CertificateException | NoSuchMethodError | VerifyError | NoClassDefFoundError e) {
            return false;
        }
    }
}
