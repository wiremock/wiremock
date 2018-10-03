package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HandlebarsRandomValuesHelperTest {

    private HandlebarsRandomValuesHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        helper = new HandlebarsRandomValuesHelper();
        transformer = new ResponseTemplateTransformer(true);

        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @Test
    public void generatesRandomAlphaNumericOfSpecifiedLength() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "length", 36
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(36));
        assertThat(output, WireMatchers.matches("^[a-z0-9]+$"));
    }

    @Test
    public void generatesUppercaseRandomAlphaNumericOfSpecifiedLength() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "length", 36,
            "uppercase", true
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(36));
        assertThat(output, WireMatchers.matches("^[A-Z0-9]+$"));
    }

    @Test
    public void generatesRandomAlphabeticOfSpecifiedLength() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "length", 43,
            "type", "ALPHABETIC",
            "uppercase", true
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(43));
        assertThat(output, WireMatchers.matches("^[A-Z]+$"));
    }

    @Test
    public void generatesRandomNumericOfSpecifiedLength() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "length", 55,
            "type", "NUMERIC"
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(55));
        assertThat(output, WireMatchers.matches("^[0-9]+$"));
    }

    @Test
    public void generatesRandomStringOfSpecifiedLength() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "length", 67,
            "type", "ALPHANUMERIC_AND_SYMBOLS"
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(67));
        assertThat(output, WireMatchers.matches("^.+$"));
    }

    @Test
    public void randomValuesCanBeAssignedToVariables() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
            mockRequest().url("/random-value"),
            aResponse()
                .withBody(
                    "{{#assign 'paymentId'}}{{randomValue length=20 type='ALPHANUMERIC' uppercase=true}}{{/assign}}\n" +
                    "{{paymentId}}\n" +
                    "{{paymentId}}"
                ).build(),
            noFileSource(),
            Parameters.empty());

        String[] bodyLines = responseDefinition.getBody().trim().split("\n");
        assertThat(bodyLines[0], is(bodyLines[1]));
        assertThat(bodyLines[0].length(), is(20));
    }

    @Test
    public void generatesRandomUUID() throws Exception {
        ImmutableMap<String, Object> optionsHash = ImmutableMap.<String, Object>of(
            "type", "UUID"
        );

        String output = render(optionsHash);

        assertThat(output.length(), is(36));
        assertThat(output, WireMatchers.matches("^[a-z0-9\\-]+$"));
    }

    private String render(ImmutableMap<String, Object> optionsHash) throws IOException {
        return helper.apply(null,
            new Options.Builder(null, null, null, null, null)
                .setHash(optionsHash).build()
        ).toString();
    }

}
