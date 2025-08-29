/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.EqualToPatternWithCaseInsensitivePrefix;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;

/**
 * A data class representing a username and password for HTTP Basic Authentication.
 *
 * <p>This class provides helpers to generate the correct {@code Authorization} header value or a
 * pattern for matching it in a stub.
 */
public class BasicCredentials {

  public final String username;
  public final String password;

  /**
   * Constructs a new BasicCredentials instance.
   *
   * @param username The username.
   * @param password The password.
   */
  @JsonCreator
  public BasicCredentials(
      @JsonProperty("username") String username, @JsonProperty("password") String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Checks if both username and password are present (non-null).
   *
   * @return true if both fields are non-null, false otherwise.
   */
  public boolean present() {
    return username != null && password != null;
  }

  /**
   * Creates a {@link MultiValuePattern} for matching an HTTP {@code Authorization} header.
   *
   * <p>The created pattern will perform a case-insensitive match for the "Basic " prefix followed
   * by an exact match of the Base64-encoded credentials.
   *
   * @return A pattern for matching the {@code Authorization} header.
   */
  public MultiValuePattern asAuthorizationMultiValuePattern() {
    return MultiValuePattern.of(
        new EqualToPatternWithCaseInsensitivePrefix("Basic ", encodedUsernameAndPassword()));
  }

  /**
   * Generates the full value for an HTTP {@code Authorization} header.
   *
   * <p>The format is "Basic " followed by the Base64-encoded "username:password".
   *
   * @return The complete {@code Authorization} header value string.
   */
  public String asAuthorizationHeaderValue() {
    return "Basic " + encodedUsernameAndPassword();
  }

  private String encodedUsernameAndPassword() {
    byte[] usernameAndPassword = (username + ":" + password).getBytes();
    return encodeBase64(usernameAndPassword);
  }
}
