package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.header;
import static org.hamcrest.Matchers.hasItems;
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

    @SuppressWarnings("unchecked")
    @Test
    public void createsCopy() {
        HttpHeaders httpHeaders = new HttpHeaders(
                httpHeader("Header-1", "h1v1", "h1v2"),
                httpHeader("Header-2", "h2v1", "h2v2"));

        HttpHeaders copyOfHeaders = HttpHeaders.copyOf(httpHeaders);

        assertThat(copyOfHeaders.all(), hasItems(
                header("Header-1", "h1v1"),
                header("Header-1", "h1v2"),
                header("Header-2", "h2v1"),
                header("Header-2", "h2v2")));
    }

}
