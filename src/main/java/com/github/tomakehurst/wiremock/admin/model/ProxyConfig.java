package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * @author Christopher Holomek
 */
public class ProxyConfig {

    private Map<String, String> proxyConfig;

    @JsonCreator
    public ProxyConfig() {
    }

    public Map<String, String> getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(Map<String, String> proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

}
