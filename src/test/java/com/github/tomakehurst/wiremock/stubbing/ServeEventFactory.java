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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class ServeEventFactory {

  public static ServeEvent newPostMatchServeEvent(
      Request request, ResponseDefinitionBuilder responseDefinitionBuilder) {
    StubMapping stubMapping =
        WireMock.any(WireMock.anyUrl()).willReturn(responseDefinitionBuilder).build();
    return newPostMatchServeEvent(request, responseDefinitionBuilder, stubMapping);
  }

  public static ServeEvent newPostMatchServeEvent(
      Request request, ResponseDefinition responseDefinition) {
    StubMapping stubMapping = WireMock.any(WireMock.anyUrl()).build();
    stubMapping.setResponse(responseDefinition);
    return newPostMatchServeEvent(request, responseDefinition, stubMapping);
  }

  public static ServeEvent newPostMatchServeEvent(
      Request request,
      ResponseDefinitionBuilder responseDefinitionBuilder,
      StubMapping stubMapping) {
    return newPostMatchServeEvent(request, responseDefinitionBuilder.build(), stubMapping);
  }

  public static ServeEvent newPostMatchServeEvent(
      Request request, ResponseDefinition responseDefinition, StubMapping stubMapping) {
    return new ServeEvent(LoggedRequest.createFrom(request), stubMapping, responseDefinition);
  }
}
