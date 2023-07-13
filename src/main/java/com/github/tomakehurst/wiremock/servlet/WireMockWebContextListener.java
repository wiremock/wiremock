/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class WireMockWebContextListener implements ServletContextListener {

  private static final String APP_CONTEXT_KEY = "WireMockApp";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();

    boolean verboseLoggingEnabled =
        Boolean.parseBoolean(
            getFirstNonNull(context.getInitParameter("verboseLoggingEnabled"), "true"));

    WireMockApp wireMockApp =
        new WireMockApp(new WarConfiguration(context), new NotImplementedContainer());

    context.setAttribute(APP_CONTEXT_KEY, wireMockApp);
    context.setAttribute(StubRequestHandler.class.getName(), wireMockApp.buildStubRequestHandler());
    context.setAttribute(
        AdminRequestHandler.class.getName(), wireMockApp.buildAdminRequestHandler());
    context.setAttribute(Notifier.KEY, new Slf4jNotifier(verboseLoggingEnabled));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
