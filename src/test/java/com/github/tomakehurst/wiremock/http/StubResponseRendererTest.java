package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class StubResponseRendererTest {
    private static final int EXECUTE_WITHOUT_SLEEP_MILLIS = 100;

    private Mockery context;
    private FileSource fileSource;
    private GlobalSettingsHolder globalSettingsHolder;
    private List<ResponseTransformer> responseTransformers;
    private StubResponseRenderer stubResponseRenderer;

    @Before
    public void init() {
        context = new Mockery();
        fileSource = context.mock(FileSource.class);
        globalSettingsHolder = new GlobalSettingsHolder();
        responseTransformers = new ArrayList<>();
        stubResponseRenderer = new StubResponseRenderer(fileSource, globalSettingsHolder, null, responseTransformers);
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void endpointFixedDelayShouldOverrideGlobalDelay() throws Exception {
        globalSettingsHolder.get().setFixedDelay(1000);

        Response response = stubResponseRenderer.render(createResponseDefinition(100));

        assertThat(response.getInitialDelay(), is(100L));
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void globalFixedDelayShouldNotBeOverriddenIfNoEndpointDelaySpecified() throws Exception {
        globalSettingsHolder.get().setFixedDelay(1000);

        Response response = stubResponseRenderer.render(createResponseDefinition(null));

        assertThat(response.getInitialDelay(), is(1000L));
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void shouldSetGlobalFixedDelayOnResponse() throws Exception {
        globalSettingsHolder.get().setFixedDelay(1000);

        Response response = stubResponseRenderer.render(createResponseDefinition(null));

        assertThat(response.getInitialDelay(), is(1000L));
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void shouldSetEndpointFixedDelayOnResponse() throws Exception {
        Response response = stubResponseRenderer.render(createResponseDefinition(2000));

        assertThat(response.getInitialDelay(), is(2000L));
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void shouldSetEndpointDistributionDelayOnResponse() throws Exception {
        globalSettingsHolder.get().setDelayDistribution(new DelayDistribution() {
            @Override
            public long sampleMillis() {
                return 123;
            }
        });

        Response response = stubResponseRenderer.render(createResponseDefinition(null));

        assertThat(response.getInitialDelay(), is(123L));
    }

    @Test(timeout = EXECUTE_WITHOUT_SLEEP_MILLIS)
    public void shouldCombineFixedDelayDistributionDelay() throws Exception {
        globalSettingsHolder.get().setDelayDistribution(new DelayDistribution() {
            @Override
            public long sampleMillis() {
                return 123;
            }
        });
        Response response = stubResponseRenderer.render(createResponseDefinition(2000));
        assertThat(response.getInitialDelay(), is(2123L));
    }

    private ResponseDefinition createResponseDefinition(Integer fixedDelayMillis) {
        return new ResponseDefinition(
                0,
                "",
                "",
                null,
                "",
                "",
                null,
                null,
                fixedDelayMillis,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
    }
}