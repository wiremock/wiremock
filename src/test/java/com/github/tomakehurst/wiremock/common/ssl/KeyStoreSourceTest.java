package com.github.tomakehurst.wiremock.common.ssl;

import org.junit.Test;

import java.security.Key;
import java.security.KeyStore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class KeyStoreSourceTest {

    @Test
    public void loadsAPasswordProtectedJksKeyStore() throws Exception {
        KeyStoreSource keyStoreSource = new ReadOnlyFileOrClasspathKeyStoreSource(
                "test-keystore-pwd",
                "jks",
                "nondefaultpass".toCharArray()
        );

        KeyStore keyStore = keyStoreSource.load();

        Key key = keyStore.getKey("server", "password".toCharArray());
        assertThat(key, notNullValue());
    }
}
