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
package com.github.tomakehurst.wiremock.http.client;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import java.io.IOException;
import java.util.List;

/** The interface Http client. */
public interface HttpClient {

  /** The constant USER_AGENT. */
  String USER_AGENT = "user-agent";

  /** The constant TRANSFER_ENCODING. */
  String TRANSFER_ENCODING = "transfer-encoding";

  /** The Forbidden response headers. */
  List<String> FORBIDDEN_RESPONSE_HEADERS = List.of(TRANSFER_ENCODING, "connection");

  /** The constant CONTENT_ENCODING. */
  String CONTENT_ENCODING = "content-encoding";

  /** The constant CONTENT_LENGTH. */
  String CONTENT_LENGTH = "content-length";

  /** The constant CONNECTION. */
  String CONNECTION = "connection";

  /** The constant UPGRADE. */
  String UPGRADE = "upgrade";

  /** The Forbidden request headers. */
  List<String> FORBIDDEN_REQUEST_HEADERS =
      List.of(TRANSFER_ENCODING, CONTENT_LENGTH, CONNECTION, UPGRADE, USER_AGENT);

  /** The constant HOST_HEADER. */
  String HOST_HEADER = "host";

  /** The constant ACCEPT_ENCODING_HEADER. */
  String ACCEPT_ENCODING_HEADER = "accept-encoding";

  /**
   * Execute response.
   *
   * @param request the request
   * @return the response
   * @throws IOException the io exception
   */
  Response execute(Request request) throws IOException;
}
