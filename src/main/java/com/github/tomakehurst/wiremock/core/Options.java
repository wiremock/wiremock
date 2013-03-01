package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.ProxySettings;

public interface Options {

    public static final int DEFAULT_PORT = 8080;

    int portNumber();
    boolean specifiesHttpsPortNumber();
    int httpsPortNumber();
    boolean browserProxyingEnabled();
    ProxySettings proxyVia();
}
