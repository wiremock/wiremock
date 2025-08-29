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
package com.github.tomakehurst.wiremock.http.client;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extension;
import java.util.List;

/** The interface Http client factory. */
public interface HttpClientFactory extends Extension {

  @Override
  default String getName() {
    return "http-client-factory";
  }

  /**
   * Build http client http client.
   *
   * @param options the options
   * @param trustAllCertificates the trust all certificates
   * @param trustedHosts the trusted hosts
   * @param useSystemProperties the use system properties
   * @return the http client
   */
  HttpClient buildHttpClient(
      Options options,
      boolean trustAllCertificates,
      List<String> trustedHosts,
      boolean useSystemProperties);
}
