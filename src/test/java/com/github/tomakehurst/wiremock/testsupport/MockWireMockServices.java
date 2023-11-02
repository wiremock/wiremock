/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import static java.util.Collections.emptyMap;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.store.Stores;
import com.google.common.base.Suppliers;
import java.util.Map;
import java.util.function.Supplier;

public class MockWireMockServices implements WireMockServices {

  private FileSource fileSource = new NoFileSource();

  private Map<String, Helper<?>> helpers = emptyMap();
  private Long maxCacheEntries = null;
  private Supplier<TemplateEngine> templateEngine =
      Suppliers.memoize(() -> new TemplateEngine(helpers, maxCacheEntries, null, false));

  @Override
  public Admin getAdmin() {
    return null;
  }

  @Override
  public Stores getStores() {
    return null;
  }

  @Override
  public FileSource getFiles() {
    return fileSource;
  }

  @Override
  public Options getOptions() {
    return null;
  }

  @Override
  public Extensions getExtensions() {
    return null;
  }

  @Override
  public TemplateEngine getTemplateEngine() {
    return templateEngine.get();
  }

  @Override
  public HttpClientFactory getHttpClientFactory() {
    return null;
  }

  @Override
  public HttpClient getDefaultHttpClient() {
    return null;
  }

  public MockWireMockServices setFileSource(FileSource fileSource) {
    this.fileSource = fileSource;
    return this;
  }

  public MockWireMockServices setHelpers(Map<String, Helper<?>> helpers) {
    this.helpers = helpers;
    return this;
  }

  public MockWireMockServices setMaxCacheEntries(Long maxCacheEntries) {
    this.maxCacheEntries = maxCacheEntries;
    return this;
  }
}
