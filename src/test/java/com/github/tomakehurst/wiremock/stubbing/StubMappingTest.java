package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.common.Json;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class StubMappingTest {

    @Test
    public void excludesInsertionIndexFromPublicView() {
        StubMapping stub = get("/saveable").willReturn(ok()).build();

        String json = Json.write(stub);
        System.out.println(json);

        assertThat(json, not(containsString("insertionIndex")));
    }

    @Test
    public void includedInsertionIndexInPrivateView() {
        StubMapping stub = get("/saveable").willReturn(ok()).build();

        String json = Json.writePrivate(stub);
        System.out.println(json);

        assertThat(json, containsString("insertionIndex"));
    }
}
