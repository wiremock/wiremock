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
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.protobuf.MessageOrBuilder;
import org.wiremock.annotations.Beta;
import org.wiremock.grpc.internal.JsonMessageUtils;

@Beta(justification = "Incubating extension: https://github.com/wiremock/wiremock/issues/2383")
public class WireMockGrpc {

  private WireMockGrpc() {}

  public static GrpcStubMappingBuilder method(String method) {
    return new GrpcStubMappingBuilder(method);
  }

  public static StringValuePattern equalToMessage(MessageOrBuilder messageOrBuilder) {
    final String json = JsonMessageUtils.toJson(messageOrBuilder);
    return WireMock.equalToJson(json, true, false);
  }

  public static GrpcResponseDefinitionBuilder json(String json) {
    return new GrpcResponseDefinitionBuilder(Status.OK).fromJson(json);
  }

  public static GrpcResponseDefinitionBuilder jsonTemplate(String json) {
    return new GrpcResponseDefinitionBuilder(Status.OK).withTemplatingEnabled(true).fromJson(json);
  }

  public static GrpcResponseDefinitionBuilder message(MessageOrBuilder messageOrBuilder) {
    final String json = JsonMessageUtils.toJson(messageOrBuilder);
    return new GrpcResponseDefinitionBuilder(Status.OK).fromJson(json);
  }

  public enum Status {
    OK(0),
    CANCELLED(1),

    UNKNOWN(2),

    INVALID_ARGUMENT(3),

    DEADLINE_EXCEEDED(4),

    NOT_FOUND(5),

    ALREADY_EXISTS(6),

    PERMISSION_DENIED(7),

    RESOURCE_EXHAUSTED(8),

    FAILED_PRECONDITION(9),

    ABORTED(10),

    OUT_OF_RANGE(11),

    UNIMPLEMENTED(12),

    INTERNAL(13),

    UNAVAILABLE(14),

    DATA_LOSS(15),

    UNAUTHENTICATED(16);

    private final int value;

    Status(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    //    public GrpcResponseDefinitionBuilder json(String json) {
    //      return new GrpcResponseDefinitionBuilder(value).fromJson(json);
    //    }
    //
    //    public GrpcResponseDefinitionBuilder message(MessageOrBuilder messageOrBuilder) {
    //      final String json =
    //          Exceptions.uncheck(() -> jsonPrinter.print(messageOrBuilder), String.class);
    //      return new GrpcResponseDefinitionBuilder(value).fromJson(json);
    //    }
  }
}
