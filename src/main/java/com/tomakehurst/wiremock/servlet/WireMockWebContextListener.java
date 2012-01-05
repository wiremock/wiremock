package com.tomakehurst.wiremock.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.tomakehurst.wiremock.WireMockApp;
import com.tomakehurst.wiremock.common.ClassPathFileSource;
import com.tomakehurst.wiremock.common.Log4jNotifier;

public class WireMockWebContextListener implements ServletContextListener {
    
    private static final String APP_CONTEXT_KEY = "WireMockApp";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ClassPathFileSource fileSource = new ClassPathFileSource("/WEB-INF/wiremock");
        WireMockApp wireMockApp = new WireMockApp(fileSource, new Log4jNotifier());
        context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
