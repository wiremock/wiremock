package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DiffSectionTest {

    @Test
    public void rendersCorrectMessageForUrl() {
        DiffSection<?> section = new DiffSection<>(
            "URL",
            new UrlPattern(WireMock.equalTo("/correct"), false),
            "",
            ""
        );

        assertThat(section.getMessage(), is("URL does not match"));
    }
}
