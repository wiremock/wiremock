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

  private final int grpcStatus;
  private String json;

  public GrpcResponseDefinitionBuilder(int grpcStatus) {
    this.grpcStatus = grpcStatus;
  }

  public GrpcResponseDefinitionBuilder fromJson(String json) {
    this.json = json;
    return this;
  }

  public ResponseDefinitionBuilder build() {
    return ResponseDefinitionBuilder.responseDefinition().withBody(json);
  }
}
