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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import com.github.tomakehurst.wiremock.verification.diff.Diff;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A subclass of {@link AssertionError} thrown when a request verification fails.
 *
 * <p>This exception provides detailed, human-readable error messages for various verification
 * failure scenarios, such as incorrect request counts or requests not matching any stub.
 */
public class VerificationException extends AssertionError {

  private static final long serialVersionUID = 5116216532516117538L;

  /**
   * Constructs a new VerificationException with a generic message.
   *
   * @param message The detail message.
   */
  public VerificationException(String message) {
    super(message);
  }

  private VerificationException(String messageStart, Diff diff) {
    super(messageStart + " " + diff.toString());
  }

  /**
   * Constructs an exception for a verification failure where at least one match was expected, but
   * none were found.
   *
   * @param expected The request pattern that was being verified.
   * @param requests The full list of requests received.
   */
  public VerificationException(RequestPattern expected, List<LoggedRequest> requests) {
    super(
        String.format(
            "Expected at least one request matching: %s\nRequests received: %s",
            expected.toString(), Json.write(requests)));
  }

  /**
   * Constructs an exception for a verification failure where the request count did not match the
   * expected strategy (e.g., "at least 5").
   *
   * @param expected The request pattern that was being verified.
   * @param expectedCount The count matching strategy (e.g., "at least 5").
   * @param actualCount The actual number of requests received.
   */
  public VerificationException(
      RequestPattern expected, CountMatchingStrategy expectedCount, int actualCount) {
    super(
        String.format(
            "Expected %s requests matching the following pattern but received %d:\n%s",
            expectedCount.toString().toLowerCase(), actualCount, expected.toString()));
  }

  /**
   * Creates an exception for one or more requests that were not matched by any stub mapping.
   *
   * @param unmatchedRequests The list of requests that were not matched.
   * @return A new {@code VerificationException}.
   */
  public static VerificationException forUnmatchedRequests(List<LoggedRequest> unmatchedRequests) {
    if (unmatchedRequests.size() == 1) {
      return new VerificationException(
          String.format(
              "A request was unmatched by any stub " + "mapping. Request was: %s",
              unmatchedRequests.get(0)));
    }

    return new VerificationException(
        unmatchedRequests.size()
            + " requests were unmatched by any stub mapping. Requests are:\n"
            + renderList(unmatchedRequests));
  }

  /**
   * Creates an exception for a verification failure where no requests matched the pattern.
   *
   * @param diff A diff showing the most similar request that was received.
   * @return A new {@code VerificationException}.
   */
  public static VerificationException forUnmatchedRequestPattern(Diff diff) {
    return new VerificationException(
        "No requests exactly matched. Most similar request was:", diff);
  }

  /**
   * Creates an exception for a single request that was not matched by any stub mapping.
   *
   * @param diff A diff showing the closest stub mapping that almost matched.
   * @return A new {@code VerificationException}.
   */
  public static VerificationException forSingleUnmatchedRequest(Diff diff) {
    return new VerificationException(
        "A request was unmatched by any stub mapping. Closest stub mapping was:", diff);
  }

  /**
   * Creates an exception for one or more requests that were not matched by any stub mapping.
   *
   * @param nearMisses A list of near misses, each containing an unmatched request and the closest
   *     stub.
   * @return A new {@code VerificationException}.
   */
  public static VerificationException forUnmatchedNearMisses(List<NearMiss> nearMisses) {
    if (nearMisses.size() == 1) {
      return forSingleUnmatchedRequest(nearMisses.get(0).getDiff());
    }

    return new VerificationException(
        nearMisses.size()
            + " requests were unmatched by any stub mapping. "
            + "Shown with closest stub mappings:\n"
            + renderList(nearMisses));
  }

  private static String renderList(List<?> list) {
    return list.stream().map(Object::toString).collect(Collectors.joining("\n\n"));
  }
}
