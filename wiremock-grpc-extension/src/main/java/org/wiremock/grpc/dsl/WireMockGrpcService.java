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
package org.wiremock.grpc.dsl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.wiremock.annotations.Beta;

@Beta(justification = "Incubating extension: https://github.com/wiremock/wiremock/issues/2383")
public class WireMockGrpcService {

  private final WireMock wireMock;
  private final String serviceName;

  public WireMockGrpcService(WireMock wireMock, String serviceName) {
    this.wireMock = wireMock;
    this.serviceName = serviceName;
  }

  public StubMapping stubFor(GrpcStubMappingBuilder builder) {
    final StubMapping stubMapping = builder.build(serviceName);
    wireMock.register(stubMapping);
    return stubMapping;
  }
}
