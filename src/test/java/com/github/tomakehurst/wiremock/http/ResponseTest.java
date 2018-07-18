package com.github.tomakehurst.wiremock.http;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.common.Gzip.gzip;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ResponseTest {

    private static final String BODY = "zipped body";

    @Test
    public void returnsUnzippedStringBodyWhenResponseBodyIsZipped() {
        Response responseWithZippedBody = new Response.Builder().body(gzip(BODY)).build();

        assertThat(responseWithZippedBody.getBodyAsString(), is(BODY));
    }

}