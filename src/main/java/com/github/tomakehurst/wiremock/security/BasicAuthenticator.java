/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.security;

import static com.github.tomakehurst.wiremock.common.ContentTypes.AUTHORIZATION;
import static java.util.Arrays.asList;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.http.Request;
import java.util.List;
import java.util.stream.Collectors;

public class BasicAuthenticator implements Authenticator {

  private final List<BasicCredentials> credentials;

  public BasicAuthenticator(List<BasicCredentials> credentials) {
    this.credentials = credentials;
  }

  public BasicAuthenticator(BasicCredentials... credentials) {
    this.credentials = asList(credentials);
  }

  public BasicAuthenticator(String username, String password) {
    this(new BasicCredentials(username, password));
  }

  @Override
  public boolean authenticate(Request request) {
    List<String> headerValues =
        credentials.stream()
            .map(BasicCredentials::asAuthorizationHeaderValue)
            .collect(Collectors.toList());
    return request.containsHeader(AUTHORIZATION)
        && headerValues.contains(request.header(AUTHORIZATION).firstValue());
  }
}
