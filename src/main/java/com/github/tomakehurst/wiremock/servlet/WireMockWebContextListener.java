/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.Log4jConfiguration;
import com.github.tomakehurst.wiremock.common.Log4jNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.common.ServletContextFileSource;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.global.NotImplementedRequestDelayControl;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WireMockWebContextListener implements ServletContextListener {

    private static final String FILES_ROOT = "__files";
    private static final String APP_CONTEXT_KEY = "WireMockApp";
    private static final String FILE_SOURCE_ROOT_KEY = "WireMockFileSourceRoot";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String fileSourceRoot = context.getInitParameter(FILE_SOURCE_ROOT_KEY);
        
        ServletContextFileSource fileSource = new ServletContextFileSource(context, fileSourceRoot);
        Log4jConfiguration.configureLogging(true);

        JsonFileMappingsLoader defaultMappingsLoader = new JsonFileMappingsLoader(fileSource.child("mappings"));
        WireMockApp wireMockApp = new WireMockApp(new NotImplementedRequestDelayControl(), false, defaultMappingsLoader, false);
        AdminRequestHandler adminRequestHandler = new AdminRequestHandler(wireMockApp, new BasicResponseRenderer());
        StubRequestHandler stubRequestHandler = new StubRequestHandler(wireMockApp,
                new StubResponseRenderer(fileSource.child(FILES_ROOT),
                        wireMockApp.getGlobalSettingsHolder(),
                        new ProxyResponseRenderer()));
        context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
        context.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
        context.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
        context.setAttribute(Notifier.KEY, new Log4jNotifier());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
