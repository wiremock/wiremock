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

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.List;
import org.wiremock.annotations.Beta;

@Beta(justification = "Incubating extension: https://github.com/wiremock/wiremock/issues/2383")
public class GrpcStubMappingBuilder {

  private String method;
  private GrpcResponseDefinitionBuilder responseBuilder;

  private List<StringValuePattern> requestMessageJsonPatterns = new ArrayList<>();

  public GrpcStubMappingBuilder(String method) {
    this.method = method;
  }

  public GrpcStubMappingBuilder withRequestMessage(StringValuePattern requestMessageJsonPattern) {
    this.requestMessageJsonPatterns.add(requestMessageJsonPattern);
    return this;
  }

  public GrpcStubMappingBuilder willReturn(GrpcResponseDefinitionBuilder responseBuilder) {
    this.responseBuilder = responseBuilder;
    return this;
  }

  public GrpcStubMappingBuilder willReturn(WireMockGrpc.Status status, String statusReason) {
    this.responseBuilder = new GrpcResponseDefinitionBuilder(status, statusReason);
    return this;
  }

  public StubMapping build(String serviceName) {
    final String path = "/" + serviceName + "/" + method;
    final MappingBuilder mappingBuilder = WireMock.post(WireMock.urlPathEqualTo(path));
    requestMessageJsonPatterns.forEach(mappingBuilder::withRequestBody);
    return mappingBuilder.willReturn(responseBuilder.build()).build();
  }
}
