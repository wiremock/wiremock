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
package com.github.tomakehurst.wiremock.admin.tasks;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static java.net.HttpURLConnection.HTTP_OK;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.HealthCheckResult;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * An admin task to provide a system health check.
 *
 * <p>This task handles the API request for the server's health status, returning key metrics like
 * status, version, and uptime.
 *
 * @see HealthCheckResult
 */
public class HealthCheckTask implements AdminTask {
  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {

    return responseDefinition()
        .withStatus(HTTP_OK)
        .withStatusMessage("Wiremock is ok")
        .withBody(
            Json.write(
                new HealthCheckResult(
                    HealthCheckStatus.HEALTHY.name().toLowerCase(), "Wiremock is ok")))
        .withHeader("Content-Type", "application/json")
        .build();
  }

  /** Represents the possible health states of the server. */
  protected enum HealthCheckStatus {
    HEALTHY,
    UNHEALTHY
  }
}
