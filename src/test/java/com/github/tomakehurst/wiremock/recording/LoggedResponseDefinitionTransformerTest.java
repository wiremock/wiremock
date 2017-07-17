package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.recording.LoggedResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.*;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static org.junit.Assert.assertEquals;

public class LoggedResponseDefinitionTransformerTest {
    private LoggedResponseDefinitionTransformer aTransformer() {
        return new LoggedResponseDefinitionTransformer();
    }

    @Test
    public void applyWithEmptyHeadersAndBody() {
        final LoggedResponse response = LoggedResponse.from(Response.response().status(401).build());
        assertEquals(responseDefinition().withStatus(401).build(), aTransformer().apply(response));
    }

    @Test
    public void applyWithTextBody() {
        final LoggedResponse response = LoggedResponse.from(Response
            .response()
            .headers(new HttpHeaders(new ContentTypeHeader("text/plain")))
            .body("foo")
            .build()
        );
        final ResponseDefinition expected = responseDefinition()
            .withHeader("Content-Type", "text/plain")
            .withBody("foo")
            .build();
        assertEquals(expected, aTransformer().apply(response));
    }

    @Test
    public void applyWithBinaryBody() {
        final byte[] body = new byte[] { 0x1, 0xc, 0x3, 0xb, 0x1 };
        final LoggedResponse response = LoggedResponse.from(Response
            .response()
            .headers(new HttpHeaders(new ContentTypeHeader("application/octet-stream")))
            .body(body)
            .build()
        );
        final ResponseDefinition expected = responseDefinition()
            .withHeader("Content-Type", "application/octet-stream")
            .withBody(body)
            .build();
        assertEquals(expected, aTransformer().apply(response));
    }

    @Test
    public void applyWithExtraHeaders() {
        final LoggedResponse response = LoggedResponse.from(Response
            .response()
            .headers(new HttpHeaders(
                HttpHeader.httpHeader("Content-Encoding", "gzip"),
                HttpHeader.httpHeader("Content-Length", "10"),
                HttpHeader.httpHeader("Accept", "application/json"),
                HttpHeader.httpHeader("X-foo", "Bar")
            ))
            .build()
        );
        final ResponseDefinition expected = responseDefinition()
            .withHeader("Accept", "application/json")
            .withHeader("X-foo", "Bar")
            .build();
        assertEquals(expected, aTransformer().apply(response));
    }
}
