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
package com.github.tomakehurst.wiremock.http.client.okhttp;

import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import java.util.Collections;
import java.util.List;

public class OkHttpClientFactory implements HttpClientFactory {

  @Override
  public String getName() {
    return "okhttp-client-factory";
  }

  @Override
  public HttpClient buildHttpClient(
      Options options,
      boolean trustAllCertificates,
      List<String> trustedHosts,
      boolean useSystemProperties) {
    final okhttp3.OkHttpClient okHttpClient =
        createClient(
            options.getMaxHttpClientConnections(),
            options.proxyTimeout(),
            options.proxyVia(),
            options.httpsSettings().trustStore(),
            trustAllCertificates,
            trustedHosts,
            useSystemProperties,
            options.getProxyTargetRules(),
            options.getDisableConnectionReuse(),
            null);

    return new OkHttpBackedHttpClient(okHttpClient, options.shouldPreserveUserAgentProxyHeader());
  }

  public static okhttp3.OkHttpClient createClient() {
    return createClient(HttpClientFactory.DEFAULT_TIMEOUT);
  }

  public static okhttp3.OkHttpClient createClient(int timeoutMilliseconds) {
    return createClient(
        HttpClientFactory.DEFAULT_MAX_CONNECTIONS,
        timeoutMilliseconds,
        NO_PROXY,
        NO_STORE,
        true,
        Collections.emptyList(),
        true,
        NetworkAddressRules.ALLOW_ALL,
        false,
        null);
  }

  public static okhttp3.OkHttpClient createClient(
      int maxConnections,
      int timeoutMilliseconds,
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      boolean trustAllCertificates,
      final List<String> trustedHosts,
      boolean useSystemProperties,
      NetworkAddressRules networkAddressRules,
      boolean disableConnectionReuse,
      String userAgent) {
    return StaticOkHttpClientFactory.createClient(
        maxConnections,
        timeoutMilliseconds,
        proxySettings,
        trustStoreSettings,
        trustAllCertificates,
        trustedHosts,
        useSystemProperties,
        networkAddressRules,
        disableConnectionReuse,
        userAgent);
  }
}
