package com.github.tomakehurst.wiremock.common;

import com.google.common.base.Preconditions;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getFirst;

public class ProxySettings {

    public static final ProxySettings NO_PROXY = new ProxySettings(null, 0);

    private final String host;
    private final int port;

    public ProxySettings(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static ProxySettings fromString(String config) {
        Iterable<String> parts = on(":").split(config);
        String host = getFirst(parts, "");
        Preconditions.checkArgument(!host.isEmpty(), "Host part of proxy must be specified");

        int port = Integer.valueOf(get(parts, 1, "80"));
        return new ProxySettings(host, port);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        if (this == NO_PROXY) {
            return "(no proxy)";
        }

        return host() + ":" + port();
    }
}
