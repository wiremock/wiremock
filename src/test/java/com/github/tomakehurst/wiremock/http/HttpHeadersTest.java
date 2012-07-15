package com.github.tomakehurst.wiremock.http;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
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

    private Matcher<HttpHeader> header(final String key, final String value) {
        return new TypeSafeMatcher<HttpHeader>() {
            @Override
            public boolean matchesSafely(HttpHeader httpHeader) {
                return httpHeader.key().equals(key) && httpHeader.containsValue(value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Header %s: %s", key, value));
            }
        };
    }
}
