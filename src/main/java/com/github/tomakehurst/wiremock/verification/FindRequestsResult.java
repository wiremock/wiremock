/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/** The type Find requests result. */
public class FindRequestsResult extends JournalBasedResult {

  private final List<LoggedRequest> requests;

  /**
   * Instantiates a new Find requests result.
   *
   * @param requests the requests
   * @param requestJournalDisabled the request journal disabled
   */
  @JsonCreator
  public FindRequestsResult(
      @JsonProperty("requests") List<LoggedRequest> requests,
      @JsonProperty("requestJournalDisabled") boolean requestJournalDisabled) {
    super(requestJournalDisabled);
    this.requests = requests;
  }

  /**
   * Gets requests.
   *
   * @return the requests
   */
  public List<LoggedRequest> getRequests() {
    return requests;
  }

  /**
   * With request journal disabled find requests result.
   *
   * @return the find requests result
   */
  public static FindRequestsResult withRequestJournalDisabled() {
    return new FindRequestsResult(Collections.emptyList(), true);
  }

  /**
   * With requests find requests result.
   *
   * @param requests the requests
   * @return the find requests result
   */
  public static FindRequestsResult withRequests(List<LoggedRequest> requests) {
    return new FindRequestsResult(requests, false);
  }
}
