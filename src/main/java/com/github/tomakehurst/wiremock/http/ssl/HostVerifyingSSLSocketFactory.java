package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HostVerifyingSSLSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;

    public HostVerifyingSSLSocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return verifyHosts(delegate.createSocket(s, host, port, autoClose));
    }

    public Socket createSocket() throws IOException {
        return verifyHosts(delegate.createSocket());
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return verifyHosts(delegate.createSocket(host, port));
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return verifyHosts(delegate.createSocket(host, port, localHost, localPort));
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return verifyHosts(delegate.createSocket(host, port));
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return verifyHosts(delegate.createSocket(address, port, localAddress, localPort));
    }

    public static Socket verifyHosts(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLParameters sslParameters = sslSocket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslSocket.setSSLParameters(sslParameters);
        }
        return socket;
    }
}
