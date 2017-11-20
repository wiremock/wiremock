package com.github.tomakehurst.wiremock.jetty9;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.BasicResponseRenderer;
import com.github.tomakehurst.wiremock.http.ResponseRenderer;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class JettyHttpServerTest {

    private Mockery context;
    private AdminRequestHandler adminRequestHandler;
    private StubRequestHandler stubRequestHandler;

    @Before
    public void init() {
        context = new Mockery();
        Admin admin = context.mock(Admin.class);

        adminRequestHandler = new AdminRequestHandler(AdminRoutes.defaults(), admin, new BasicResponseRenderer(), new NoAuthenticator(), false);
        stubRequestHandler = new StubRequestHandler(context.mock(StubServer.class),
                context.mock(ResponseRenderer.class),
                admin,
                Collections.<String, PostServeAction>emptyMap(),
                context.mock(RequestJournal.class));
    }

    @Test
    public void testStopTimeout() {
        long expectedStopTimeout = 500L;
        WireMockConfiguration config = WireMockConfiguration
                .wireMockConfig()
                .jettyStopTimeout(expectedStopTimeout);

        JettyHttpServer jettyHttpServer = new JettyHttpServer(config, adminRequestHandler, stubRequestHandler);

        assertThat(jettyHttpServer.stopTimeout(), is(expectedStopTimeout));
    }

    @Test
    public void testStopTimeoutNotSet() {
        long expectedStopTimeout = 0L;
        WireMockConfiguration config = WireMockConfiguration.wireMockConfig();

        JettyHttpServer jettyHttpServer = new JettyHttpServer(config, adminRequestHandler, stubRequestHandler);

        assertThat(jettyHttpServer.stopTimeout(), is(expectedStopTimeout));
    }
}