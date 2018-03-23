package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class XmlHandlingAcceptanceTest {

    @Rule
    public WireMockRule wm = new WireMockRule(options().dynamicPort().extensions(new ResponseTemplateTransformer(false)));

    @Rule
    public WireMockRule externalDtdServer = new WireMockRule(options().dynamicPort().notifier(new ConsoleNotifier(true)));

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());

        externalDtdServer.stubFor(get("/dodgy.dtd").willReturn(ok(
            "<!ELEMENT shiftydata (#PCDATA)>")
            .withHeader("Content-Type", "application/xml-dtd"))
        );
    }

    @Test
    public void doesNotDownloadExternalDtdDocumentsWhenMatchingOnEqualToXml() {
        String xml =
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE things [\n" +
            "<!ENTITY % sp SYSTEM \"http://localhost:" + externalDtdServer.port() + "/dodgy.dtd\">\n" +
            "%sp;\n" +
            "]>\n" +
            "\n" +
            "<things><shiftydata>123</shiftydata></things>";

        wm.stubFor(post("/xml-match")
            .withRequestBody(equalToXml(xml))
            .willReturn(ok()));

        assertThat(client.postXml("/xml-match", xml).statusCode(), is(200));

        externalDtdServer.verify(0, getRequestedFor(anyUrl()));
    }

    @Test
    public void doesNotDownloadExternalDtdDocumentsWhenMatchingXPath() {
        String xml =
            "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE things [\n" +
                "<!ENTITY % sp SYSTEM \"http://localhost:" + externalDtdServer.port() + "/dodgy.dtd\">\n" +
                "%sp;\n" +
                "]>\n" +
                "\n" +
                "<things><shiftydata>123</shiftydata></things>";

        wm.stubFor(post("/xpath-match")
            .withRequestBody(matchingXPath("//shiftydata"))
            .willReturn(ok()));

        assertThat(client.postXml("/xpath-match", xml).statusCode(), is(200));

        externalDtdServer.verify(0, getRequestedFor(anyUrl()));
    }

    @Test
    public void doesNotDownloadExternalDtdDocumentsWhenEvaluatingXPathInTemplate() {
        String xml =
            "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE things [\n" +
                "<!ENTITY % sp SYSTEM \"http://localhost:" + externalDtdServer.port() + "/dodgy.dtd\">\n" +
                "%sp;\n" +
                "]>\n" +
                "\n" +
                "<things><shiftydata>123</shiftydata></things>";

        wm.stubFor(post("/xpath-template")
            .willReturn(
                ok("{{xPath request.body '//shiftydata/text()'}}")
                .withTransformers(ResponseTemplateTransformer.NAME)));

        assertThat(client.postXml("/xpath-template", xml).statusCode(), is(200));

        externalDtdServer.verify(0, getRequestedFor(anyUrl()));
    }
}
