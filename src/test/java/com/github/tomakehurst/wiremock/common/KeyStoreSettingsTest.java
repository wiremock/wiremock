package com.github.tomakehurst.wiremock.common;

import org.junit.Test;

import java.security.KeyStore;

import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static org.junit.Assert.assertNotNull;

public class KeyStoreSettingsTest {

    private final static String TRUSTSTORE_PASSWORD = "mytruststorepassword";

    @Test
    public void loadsTrustStoreFromClasspath() {
        KeyStoreSettings trustStoreSettings = new KeyStoreSettings("test-clientstore", TRUSTSTORE_PASSWORD);

        KeyStore keyStore = trustStoreSettings.loadStore();
        assertNotNull(keyStore);
    }

    @Test
    public void loadsTrustStoreFromFilesystem() {
        KeyStoreSettings trustStoreSettings = new KeyStoreSettings(filePath("test-clientstore"), TRUSTSTORE_PASSWORD);

        KeyStore keyStore = trustStoreSettings.loadStore();
        assertNotNull(keyStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsWhenTrustStoreNotFound() {
        KeyStoreSettings trustStoreSettings = new KeyStoreSettings("test-unknownstore", "");
        trustStoreSettings.loadStore();
    }

}
