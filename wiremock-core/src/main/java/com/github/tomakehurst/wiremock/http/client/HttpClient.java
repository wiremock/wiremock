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

public interface HttpClient {

  String USER_AGENT = "user-agent";
  String TRANSFER_ENCODING = "transfer-encoding";
  List<String> FORBIDDEN_RESPONSE_HEADERS = List.of(TRANSFER_ENCODING, "connection");
  String CONTENT_ENCODING = "content-encoding";
  String CONTENT_LENGTH = "content-length";
  String CONNECTION = "connection";
  String UPGRADE = "upgrade";
  List<String> FORBIDDEN_REQUEST_HEADERS =
      List.of(TRANSFER_ENCODING, CONTENT_LENGTH, CONNECTION, UPGRADE, USER_AGENT);
  String HOST_HEADER = "host";
  String ACCEPT_ENCODING_HEADER = "accept-encoding";

  Response execute(Request request) throws IOException;
}
