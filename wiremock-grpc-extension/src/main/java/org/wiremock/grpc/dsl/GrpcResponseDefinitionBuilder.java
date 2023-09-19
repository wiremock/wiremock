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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

public class GrpcResponseDefinitionBuilder {

  public static final String GRPC_STATUS_NAME = "grpc-status-name";
  public static final String GRPC_STATUS_REASON = "grpc-status-reason";
  private final WireMockGrpc.Status grpcStatus;
  private final String statusReason;
  private String json;

  public GrpcResponseDefinitionBuilder(WireMockGrpc.Status grpcStatus) {
    this(grpcStatus, null);
  }

  public GrpcResponseDefinitionBuilder(WireMockGrpc.Status grpcStatus, String statusReason) {
    this.grpcStatus = grpcStatus;
    this.statusReason = statusReason;
  }

  public GrpcResponseDefinitionBuilder fromJson(String json) {
    this.json = json;
    return this;
  }

  public ResponseDefinitionBuilder build() {
    final ResponseDefinitionBuilder responseDefinitionBuilder =
        ResponseDefinitionBuilder.responseDefinition()
            .withHeader(GRPC_STATUS_NAME, grpcStatus.name());

    if (statusReason != null) {
      responseDefinitionBuilder.withHeader(GRPC_STATUS_REASON, statusReason);
    }

    return responseDefinitionBuilder.withBody(json);
  }
}
