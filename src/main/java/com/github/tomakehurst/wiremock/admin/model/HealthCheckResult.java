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
package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.core.Version;
import java.time.Instant;

/**
 * A data transfer object (DTO) representing the result of a system health check.
 *
 * <p>This class encapsulates key health metrics such as status, message, version, and uptime,
 * typically returned by a health check API endpoint.
 */
public class HealthCheckResult {
  private final String status;
  private final String message;
  private final String version;
  private final Instant timestamp;

  /**
   * Constructs a new HealthCheckResult.
   *
   * <p>The version, timestamp, and uptime are automatically populated upon creation.
   *
   * @param status The health status, e.g., "OK".
   * @param message A descriptive message about the health status.
   */
  public HealthCheckResult(String status, String message) {
    this.status = status;
    this.message = message;
    this.version = Version.getCurrentVersion();
    this.timestamp = Instant.now();
  }

  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getVersion() {
    return version;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
