package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.FluentIterable.from;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EditStubMappingAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void canEditAnExistingStubMapping() {
        UUID id = UUID.randomUUID();

        wireMockServer.stubFor(get(urlEqualTo("/edit-this"))
            .withId(id)
            .willReturn(aResponse()
                .withBody("Original")));

        assertThat(testClient.get("/edit-this").content(), is("Original"));

        wireMockServer.editStub(get(urlEqualTo("/edit-this"))
            .withId(id)
            .willReturn(aResponse()
                .withBody("Modified")));

        assertThat(testClient.get("/edit-this").content(), is("Modified"));

        int editThisStubCount =
            from(wireMockServer.listAllStubMappings().getMappings())
            .filter(withUrl("/edit-this"))
            .size();

        assertThat(editThisStubCount, is(1));
    }

    private Predicate<StubMapping> withUrl(final String url) {
        return new Predicate<StubMapping>() {
            public boolean apply(StubMapping mapping) {
                return (mapping.getRequest().getUrl() != null && mapping.getRequest().getUrl().equals(url));
            }
        };
    }


}
