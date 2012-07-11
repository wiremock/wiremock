package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HttpHeaderTest {

    @Test
    public void returnsIsPresentFalseWhenNoValuesPresent() {
        HttpHeader header = HttpHeader.absent("Test-Header");
        assertThat(header.isPresent(), is(false));
    }

    @Test
    public void returnsIsPresentTrueWhenOneValuePresent() {
        HttpHeader header = new HttpHeader("Test-Header", "value");
        assertThat(header.isPresent(), is(true));
    }

    @Test
    public void returnsFirstValueWhenOneSpecified() {
        HttpHeader header = new HttpHeader("Test-Header", "value");
        assertThat(header.firstValue(), is("value"));
    }

    @Test
    public void returnsAllValuesWhenManySpecified() {
        HttpHeader header = new HttpHeader("Test-Header", "value1", "value2", "value3");
        assertThat(header.values(), hasItems("value1", "value2", "value3"));
    }

    @Test(expected=IllegalStateException.class)
    public void throwsExceptionWhenAttemptingToAccessFirstValueWhenAbsent() {
        HttpHeader.absent("Something").firstValue();
    }

    @Test(expected=IllegalStateException.class)
    public void throwsExceptionWhenAttemptingToAccessValuesWhenAbsent() {
        HttpHeader.absent("Something").values();
    }
}
