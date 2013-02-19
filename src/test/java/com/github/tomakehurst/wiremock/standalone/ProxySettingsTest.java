package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProxySettingsTest {

    @Test(expected=IllegalArgumentException.class)
    public void throwsExceptionWhenHostPartNotSpecified() {
        ProxySettings.fromString(":8090");
    }

    @Test
    public void defaultsToPort80() {
        assertThat(ProxySettings.fromString("myhost.com").port(), is(80));
    }

    @Test
    public void parsesHostAndPortCorrectly() {
        ProxySettings settings = ProxySettings.fromString("some.host.org:8888");
        assertThat(settings.host(), is("some.host.org"));
        assertThat(settings.port(), is(8888));
    }
}
