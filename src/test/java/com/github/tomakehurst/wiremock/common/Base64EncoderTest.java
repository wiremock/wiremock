/*
 * Copyright (C) 2018-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class Base64EncoderTest {
  public static final String INPUT = "1234";
  public static final String OUTPUT = "MTIzNA==";

  @Test
  void testEncoder() {
    var encoder = new JdkBase64Encoder();

    String encoded = encoder.encode(INPUT.getBytes());
    assertThat(encoded, is(OUTPUT));

    String decoded = new String(encoder.decode(encoded));
    assertThat(decoded, is(INPUT));
  }
}
