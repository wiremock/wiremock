package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.mapping.ValuePattern;
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

    @Test
    public void correctlyIndicatesWhenHeaderContainsValue() {
        HttpHeader header = new HttpHeader("Test-Header", "value1", "value2", "value3");
        assertThat(header.containsValue("value2"), is(true));
        assertThat(header.containsValue("value72727"), is(false));
    }

    @Test(expected=IllegalStateException.class)
    public void throwsExceptionWhenAttemptingToAccessFirstValueWhenAbsent() {
        HttpHeader.absent("Something").firstValue();
    }

    @Test(expected=IllegalStateException.class)
    public void throwsExceptionWhenAttemptingToAccessValuesWhenAbsent() {
        HttpHeader.absent("Something").values();
    }

    @Test
    public void shouldMatchSingleValueToValuePattern() {
        HttpHeader header = new HttpHeader("My-Header", "my-value");

        assertThat(header.hasValueMatching(ValuePattern.equalTo("my-value")), is(true));
        assertThat(header.hasValueMatching(ValuePattern.equalTo("other-value")), is(false));
    }

    @Test
    public void shouldMatchMultiValueToValuePattern() {
        HttpHeader header = new HttpHeader("My-Header", "value1", "value2", "value3");

        assertThat(header.hasValueMatching(ValuePattern.matches("value.*")), is(true));
        assertThat(header.hasValueMatching(ValuePattern.equalTo("value2")), is(true));
        assertThat(header.hasValueMatching(ValuePattern.equalTo("value4")), is(false));
    }
}
