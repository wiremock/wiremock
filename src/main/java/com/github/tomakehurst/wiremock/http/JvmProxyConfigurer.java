package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class JvmProxyConfigurer {

    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
    private static final List<String> ALL_PROXY_SETTINGS = asList(HTTP_PROXY_HOST, HTTP_PROXY_PORT, HTTPS_PROXY_HOST, HTTPS_PROXY_PORT, HTTP_NON_PROXY_HOSTS);

    private static final Map<String, String> previousSettings = new HashMap<>();

    public static void configureFor(WireMockServer wireMockServer) {
        stashPreviousSettings();

        System.setProperty(HTTP_PROXY_HOST, "localhost");
        System.setProperty(HTTP_PROXY_PORT, String.valueOf(wireMockServer.port()));
        System.setProperty(HTTPS_PROXY_HOST, "localhost");
        System.setProperty(HTTPS_PROXY_PORT, String.valueOf(wireMockServer.port()));
        System.setProperty(HTTP_NON_PROXY_HOSTS, "localhost|127.*|[::1]");
    }

    public static void restorePrevious() {
        ALL_PROXY_SETTINGS.forEach(key -> {
            final String previous = previousSettings.get(key);
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        });
    }

    private static void stashPreviousSettings() {
        ALL_PROXY_SETTINGS.forEach(key -> previousSettings.put(key, System.getProperty(key)));
    }
}
