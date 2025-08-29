/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.store.Stores;

/** The interface Wire mock services. */
public interface WireMockServices {

  /**
   * Gets admin.
   *
   * @return the admin
   */
  Admin getAdmin();

  /**
   * Gets stores.
   *
   * @return the stores
   */
  Stores getStores();

  /**
   * Gets files.
   *
   * @return the files
   */
  FileSource getFiles();

  /**
   * Gets options.
   *
   * @return the options
   */
  Options getOptions();

  /**
   * Gets extensions.
   *
   * @return the extensions
   */
  Extensions getExtensions();

  /**
   * Gets template engine.
   *
   * @return the template engine
   */
  TemplateEngine getTemplateEngine();

  HttpClientFactory getHttpClientFactory();

  /**
   * Gets default http client.
   *
   * @return the default http client
   */
  HttpClient getDefaultHttpClient();
}
