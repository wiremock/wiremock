/*
 * Copyright (C) 2020-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import javax.net.ssl.SSLException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisabledForJreRange(
    min = JRE.JAVA_17,
    disabledReason = "does not support generating certificates at runtime")
public class HttpClientFactoryRejectsUntrustedCertificatesTest
    extends HttpClientFactoryCertificateVerificationTest {
  public static Collection<Object[]> data() {
    return asList(
        new Object[][] {
          // trusted                     certificateCN validCertificate?
          {TRUST_NOBODY, "other.com", true},
          {TRUST_NOBODY, "other.com", false},
          {TRUST_NOBODY, "localhost", false},
          {singletonList("other.com"), "other.com", true},
          {singletonList("other.com"), "other.com", false},
          {singletonList("other.com"), "localhost", false}
        });
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: trusted={0}, certificateCN={1}, validCertificate={2}")
  void certificatesAreRejectedAsExpected(
      List<String> trustedHosts, String certificateCN, boolean validCertificate) throws Exception {

    startServerAndBuildClient(trustedHosts, certificateCN, validCertificate);

    server.stubFor(get("/whatever").willReturn(aResponse().withBody("Hello World")));

    assertThrows(SSLException.class, () -> client.execute(new HttpGet(server.url("/whatever"))));
  }
}
