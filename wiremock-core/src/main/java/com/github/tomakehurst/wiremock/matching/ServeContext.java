/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.Request;
import java.util.Map;

public class ServeContext {

  private final WireMockServices services;
  private final Lazy<Map<String, Object>> model;

  public ServeContext(WireMockServices services, Request request) {
    this.services = services;
    this.model = Lazy.lazy(() -> services.getTemplateEngine().buildModelForRequest(request));
  }

  public String renderTemplate(String template) {
    TemplateEngine templateEngine = services.getTemplateEngine();
    return templateEngine.getUncachedTemplate(template).apply(model.get());
  }

  public WireMockServices getServices() {
    return services;
  }
}
