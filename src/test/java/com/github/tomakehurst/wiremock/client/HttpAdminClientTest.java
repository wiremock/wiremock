/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class HttpAdminClientTest {

  @Test
  void returnsOptionsWhenCallingGetOptions() {
    HttpAdminClient client = new HttpAdminClient("localhost", 8080);
    assertThat(client.getOptions().portNumber(), is(8080));
    assertThat(client.getOptions().bindAddress(), is("localhost"));
  }
}
