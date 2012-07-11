package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpHeadersTest {

    @Test
    public void returnsAbsentHttpHeaderWhenHeaderNotPresent() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpHeader header = httpHeaders.getHeader("Test-Header");

        assertThat(header.isPresent(), is(false));
    }

    @Test
    public void returnsHeaderWhenPresent() {
        HttpHeaders httpHeaders = new HttpHeaders(httpHeader("Test-Header", "value1", "value2"));
        HttpHeader header = httpHeaders.getHeader("Test-Header");

        assertThat(header.isPresent(), is(true));
        assertThat(header.key(), is("Test-Header"));
        assertThat(header.containsValue("value2"), is(true));
    }
}
