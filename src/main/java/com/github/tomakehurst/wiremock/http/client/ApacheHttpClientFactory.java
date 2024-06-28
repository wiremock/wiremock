/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
import java.util.List;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public class ApacheHttpClientFactory implements HttpClientFactory {

  @Override
  public HttpClient buildHttpClient(
      Options options,
      boolean trustAllCertificates,
      List<String> trustedHosts,
      boolean useSystemProperties) {
    final CloseableHttpClient apacheClient =
        com.github.tomakehurst.wiremock.http.HttpClientFactory.createClient(
            options.getMaxHttpClientConnections(),
            options.proxyTimeout(),
            options.proxyVia(),
            options.httpsSettings().trustStore(),
            trustAllCertificates,
            trustedHosts,
            useSystemProperties,
            options.getProxyTargetRules(),
            options.getDisableConnectionReuse());

    return new ApacheBackedHttpClient(apacheClient, options.shouldPreserveUserAgentProxyHeader());
  }
}
