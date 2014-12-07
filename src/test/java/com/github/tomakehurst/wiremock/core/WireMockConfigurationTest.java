package com.github.tomakehurst.wiremock.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WireMockConfigurationTest {
    @Test
    public void expectToSetAndGetHttpsSettings() {
        final String keystorePath = "keystore-path";
        final String keystorePassword = "keystore-password";
        final String truststorePath = "truststore-path";
        final String truststorePassword = "truststore-password";
        final int httpsPort = 8443;
        final boolean needClientAuth = true;

        final WireMockConfiguration configuration = new WireMockConfiguration();
        configuration.httpsPort(httpsPort);
        configuration.needClientAuth(needClientAuth);
        configuration.keystore(keystorePath);
        configuration.keyPassword(keystorePassword);
        configuration.truststore(truststorePath);
        configuration.trustPassword(truststorePassword);


        assertThat(configuration.httpsSettings().enabled(), equalTo(true));
        assertThat(configuration.httpsSettings().port(), equalTo(httpsPort));
        assertThat(configuration.httpsSettings().needClientAuth(), equalTo(needClientAuth));
        assertThat(configuration.httpsSettings().keystore(), equalTo(keystorePath));
        assertThat(configuration.httpsSettings().keyPassword(), equalTo(keystorePassword));
        assertThat(configuration.httpsSettings().truststore(), equalTo(truststorePath));
        assertThat(configuration.httpsSettings().trustPassword(), equalTo(truststorePassword));
    }

    @Test
    public void expectNoHttpsByDefault() {
        final WireMockConfiguration configuration = new WireMockConfiguration();
        assertThat(configuration.httpsSettings().enabled(), equalTo(false));
    }
}