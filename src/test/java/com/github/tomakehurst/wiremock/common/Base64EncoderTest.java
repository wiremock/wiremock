package com.github.tomakehurst.wiremock.common;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class Base64EncoderTest {
    public static final String INPUT = "1234";
    public static final String OUTPUT = "MTIzNA==";

    @Test
    public void testDatatypeConverterEncoder() {
        Base64Encoder encoder = new DatatypeConverterBase64Encoder();

        String encoded = encoder.encode(INPUT.getBytes());
        assertThat(encoded, is(OUTPUT));

        String decoded = new String(encoder.decode(encoded));
        assertThat(decoded, is(INPUT));
    }

    @Test
    public void testGuavaEncoder() {
        Base64Encoder encoder = new GuavaBase64Encoder();

        String encoded = encoder.encode(INPUT.getBytes());
        assertThat(encoded, is(OUTPUT));

        String decoded = new String(encoder.decode(encoded));
        assertThat(decoded, is(INPUT));
    }
}
