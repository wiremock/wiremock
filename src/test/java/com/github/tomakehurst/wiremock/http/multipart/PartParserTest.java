/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.multipart;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class PartParserTest {

  @AfterEach
  void clearNotifier() {
    LocalNotifier.set(null);
  }

  @Test
  void reportsTheParseFailureWhenReturningNoParts() {
    Notifier notifier = mock(Notifier.class);
    LocalNotifier.set(notifier);
    Request request = mock(Request.class);
    when(request.getHeaders())
        .thenReturn(new HttpHeaders(new HttpHeader("Content-Type", "multipart/form-data")));
    when(request.getBody()).thenReturn("not a multipart body".getBytes(UTF_8));

    assertTrue(PartParser.parseFrom(request).isEmpty());

    verify(notifier)
        .error(
            "Failed to parse multipart request body, treating it as having no parts: "
                + "the request was rejected because no multipart boundary was found");
  }
}
