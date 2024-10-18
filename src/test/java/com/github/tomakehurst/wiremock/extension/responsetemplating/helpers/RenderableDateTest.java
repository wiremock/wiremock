/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.Json;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class RenderableDateTest {

  @Test
  void writesToJsonInStringFormat() {
    RenderableDate renderableDate = new RenderableDate(new Date(1729266504000L), null, null);
    assertThat(Json.write(renderableDate), is("\"2024-10-18T15:48:24Z\""));
  }
}
