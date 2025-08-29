/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.FindServeEventsResult;

/**
 * An admin task to remove serve events whose originating stub has matching metadata.
 *
 * <p>This task handles the API request to find and remove logged requests. The filtering is based
 * on a {@link StringValuePattern} provided in the request body, which is matched against the
 * metadata of the stub mapping that served each request.
 *
 * @see StringValuePattern
 * @see ServeEvent
 * @see FindServeEventsResult
 */
public class RemoveServeEventsByStubMetadataTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    StringValuePattern metadataPattern =
        Json.read(serveEvent.getRequest().getBodyAsString(), StringValuePattern.class);
    FindServeEventsResult findServeEventsResult =
        admin.removeServeEventsForStubsMatchingMetadata(metadataPattern);
    return ResponseDefinition.okForJson(findServeEventsResult);
  }
}
