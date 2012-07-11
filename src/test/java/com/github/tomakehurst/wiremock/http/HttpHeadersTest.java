package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpHeadersTest {

    @Test
    public void returnsAbsentHttpHeaderWhenHeaderNotPresent() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpHeader header = httpHeaders.getHeader("Test-Header");

        assertThat(header.isPresent(), is(false));
    }
}
