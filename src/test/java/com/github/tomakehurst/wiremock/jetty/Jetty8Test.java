package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Ignore
public class Jetty8Test {

    private WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(8099);
    }

    @Test
    public void canStartJettyWithSimpleServlet() {
        Server server = new Server(8099);

        HandlerCollection handlers = new HandlerCollection();
        ServletContextHandler handler1 = new ServletContextHandler();
        handler1.addServlet(TestServlet.class, "/*");


        server.setHandler(handlers);
    }

    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setStatus(200);
            resp.getWriter().write("Test body content");
        }
    }
}
