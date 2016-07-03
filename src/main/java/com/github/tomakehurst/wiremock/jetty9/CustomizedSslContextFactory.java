package com.github.tomakehurst.wiremock.jetty9;


import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by agupt13 on 6/9/16.
 *
 * This class is customized because the Android JVM classes do not support the `setEndpointIdentificationAlgorithm` for https communication.
 */
public class CustomizedSslContextFactory extends org.eclipse.jetty.util.ssl.SslContextFactory  {



    public void customize(SSLEngine sslEngine)
    {
        SSLParameters sslParams = sslEngine.getSSLParameters();
        // sslParams.setEndpointIdentificationAlgorithm(_endpointIdentificationAlgorithm);
        sslEngine.setSSLParameters(sslParams);

        if (super.getWantClientAuth())
            sslEngine.setWantClientAuth(super.getWantClientAuth());
        if (super.getNeedClientAuth())
            sslEngine.setNeedClientAuth(super.getNeedClientAuth());

        sslEngine.setEnabledCipherSuites(super.selectCipherSuites(
                sslEngine.getEnabledCipherSuites(),
                sslEngine.getSupportedCipherSuites()));

        sslEngine.setEnabledProtocols(super.selectProtocols(sslEngine.getEnabledProtocols(),sslEngine.getSupportedProtocols()));
    }



}