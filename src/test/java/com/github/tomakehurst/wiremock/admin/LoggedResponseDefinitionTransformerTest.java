package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.LoggedResponseDefinitionTransformer;
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
        LoggedResponse response = LoggedResponse.from(Response.response().status(401).build());
        assertEquals(responseDefinition().withStatus(401).build(), aTransformer().apply(response));
    }

    @Test
    public void applyWithEmptyHeadersAndRegularBody() {
        LoggedResponse response = LoggedResponse.from(Response.response().body("foo").build());
        ResponseDefinition expected = responseDefinition().withBody("foo").build();
        assertEquals(expected, aTransformer().apply(response));
    }

    @Test
    public void applyWithHeaders() {
        LoggedResponse response = LoggedResponse.from(Response
            .response()
            .headers(new HttpHeaders(
                HttpHeader.httpHeader("Content-Encoding", "gzip"),
                HttpHeader.httpHeader("Content-Length", "10"),
                HttpHeader.httpHeader("Accept", "application/json"),
                HttpHeader.httpHeader("X-foo", "Bar")
            ))
            .build()
        );
        ResponseDefinition expected = responseDefinition()
            .withHeader("Accept", "application/json")
            .withHeader("X-foo", "Bar")
            .build();
        assertEquals(expected, aTransformer().apply(response));
    }
}
