/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
package ignored;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JUnit5ProxyTest {

  @RegisterExtension
  public WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(options().dynamicPort().enableBrowserProxying(true))
          .build();

  CloseableHttpClient httpClient =
      HttpClientBuilder.create()
          .useSystemProperties() // This must be enabled for auto-configuration of proxy settings to
          // work
          .build();

  @BeforeEach
  public void init() {
    JvmProxyConfigurer.configureFor(wm.getPort());
  }

  @AfterEach
  public void cleanup() {
    JvmProxyConfigurer.restorePrevious();
  }

  @Test
  public void testViaProxyUsingRule() throws Exception {
    wm.stubFor(get("/things").withHost(equalTo("my.first.domain")).willReturn(ok("Domain 1")));

    wm.stubFor(get("/things").withHost(equalTo("my.second.domain")).willReturn(ok("Domain 2")));

    ClassicHttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
    String responseBody = EntityUtils.toString(response.getEntity());
    assertEquals("Domain 1", responseBody);

    response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
    responseBody = EntityUtils.toString(response.getEntity());
    assertEquals("Domain 2", responseBody);
  }

  @Test
  public void testViaProxyUsingServer() throws Exception {
    WireMockServer wireMockServer =
        new WireMockServer(options().dynamicPort().enableBrowserProxying(true));
    wireMockServer.start();
    JvmProxyConfigurer.configureFor(wireMockServer);

    wireMockServer.stubFor(
        get("/things").withHost(equalTo("my.first.domain")).willReturn(ok("Domain 1")));

    wireMockServer.stubFor(
        get("/things").withHost(equalTo("my.second.domain")).willReturn(ok("Domain 2")));

    ClassicHttpResponse response = httpClient.execute(new HttpGet("http://my.first.domain/things"));
    String responseBody = EntityUtils.toString(response.getEntity());
    assertEquals("Domain 1", responseBody);

    response = httpClient.execute(new HttpGet("http://my.second.domain/things"));
    responseBody = EntityUtils.toString(response.getEntity());
    assertEquals("Domain 2", responseBody);

    wireMockServer.close();
    JvmProxyConfigurer.restorePrevious();
  }
}
