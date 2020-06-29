package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public final class BrowserProxySettings {

    public static final String DEFAULT_CA_KEYSTORE_PATH = Paths.get(
            System.getProperty("user.home"))
            .resolve(".wiremock")
            .resolve("ca-keystore.jks")
            .toFile().getAbsolutePath();
    public static final String DEFAULT_CA_KESTORE_PASSWORD = "password";

    public static BrowserProxySettings DISABLED = new Builder().build();

    private final boolean enabled;
    private final boolean trustAllProxyTargets;
    private final List<String> trustedProxyTargets;
    private final String caKeyStorePath;
    private final String caKeyStorePassword;
    private final String caKeyStoreType;

    public BrowserProxySettings(
        boolean enabled,
        boolean trustAllProxyTargets,
        List<String> trustedProxyTargets,
        String caKeyStorePath,
        String caKeyStorePassword,
        String caKeyStoreType
    ) {
        this.enabled = enabled;
        this.trustAllProxyTargets = trustAllProxyTargets;
        this.trustedProxyTargets = trustedProxyTargets;
        this.caKeyStorePath = caKeyStorePath;
        this.caKeyStorePassword = caKeyStorePassword;
        this.caKeyStoreType = caKeyStoreType;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean trustAllProxyTargets() {
        return trustAllProxyTargets;
    }

    public List<String> trustedProxyTargets() {
        return trustedProxyTargets;
    }

    public KeyStoreSettings caKeyStore() {
        return caKeyStorePath != null ?
                new KeyStoreSettings(caKeyStorePath, caKeyStorePassword, caKeyStoreType) :
                KeyStoreSettings.NO_STORE;
    }

    @Override
    public String toString() {
        return "BrowserProxySettings{" +
                "enabled=" + enabled +
                ", trustAllProxyTargets=" + trustAllProxyTargets +
                ", trustedProxyTargets=" + trustedProxyTargets +
                ", caKeyStorePath='" + caKeyStorePath + '\'' +
                ", caKeyStoreType='" + caKeyStorePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrowserProxySettings that = (BrowserProxySettings) o;
        return enabled == that.enabled &&
                trustAllProxyTargets == that.trustAllProxyTargets &&
                Objects.equals(trustedProxyTargets, that.trustedProxyTargets) &&
                Objects.equals(caKeyStorePath, that.caKeyStorePath) &&
                Objects.equals(caKeyStorePassword, that.caKeyStorePassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStorePath, caKeyStorePassword);
    }

    public static final class Builder {

        private boolean enabled = false;
        private boolean trustAllProxyTargets = false;
        private List<String> trustedProxyTargets = emptyList();
        private String caKeyStorePath = DEFAULT_CA_KEYSTORE_PATH;
        private String caKeyStorePassword = DEFAULT_CA_KESTORE_PASSWORD;
        private String caKeyStoreType = "jks";

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder trustAllProxyTargets(boolean trustAllProxyTargets) {
            this.trustAllProxyTargets = trustAllProxyTargets;
            return this;
        }

        public Builder trustedProxyTargets(List<String> trustedProxyTargets) {
            this.trustedProxyTargets = trustedProxyTargets;
            return this;
        }

        public Builder caKeyStorePath(String caKeyStorePath) {
            this.caKeyStorePath = caKeyStorePath;
            return this;
        }

        public Builder caKeyStorePassword(String caKeyStorePassword) {
            this.caKeyStorePassword = caKeyStorePassword;
            return this;
        }

        public Builder caKeyStoreType(String caKeyStoreType) {
            this.caKeyStoreType = caKeyStoreType;
            return this;
        }

        public BrowserProxySettings build() {
            return new BrowserProxySettings(enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStorePath, caKeyStorePassword, caKeyStoreType);
        }
    }
}
