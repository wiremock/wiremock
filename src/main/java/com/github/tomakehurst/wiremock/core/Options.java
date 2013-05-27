package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;

public interface Options {

    public static final int DEFAULT_PORT = 8080;

    int portNumber();
    HttpsSettings httpsSettings();
    boolean browserProxyingEnabled();
    ProxySettings proxyVia();
    FileSource filesRoot();
    Notifier notifier();
    boolean requestJournalDisabled();

}
