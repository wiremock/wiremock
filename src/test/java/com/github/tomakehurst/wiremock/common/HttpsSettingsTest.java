package com.github.tomakehurst.wiremock.common;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HttpsSettingsTest {

    @Test
    public void expectHttpEnableReturnFalseForNoHttpsSettings() {
        assertThat(HttpsSettings.NO_HTTPS.enabled(), equalTo(false));
    }

    @Test
    public void expectHttpEnableReturnTrueForValidSettings() {
        final HttpsSettings settings = new HttpsSettings(8443, "keystore_path", "keystore_password",
                "truststore_path", "truststore_password", true);
        assertThat(settings.enabled(), equalTo(true));
    }
}