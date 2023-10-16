/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ServerNameInLogsAcceptanceTest extends AcceptanceTestBase {

  public static final String SERVER_NAME = "AcceptanceTestServer";

  @BeforeAll
  public static void setupServer() {
    setupServer(options().serverName(SERVER_NAME));
  }

  @Test
  public void checkServerNameIsPrintedInLogs() {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    System.setOut(new PrintStream(byteArrayOutputStream));
    testClient.get("/time-me");
    String logs = byteArrayOutputStream.toString();
    assertTrue(logs.contains(SERVER_NAME));
  }
}
