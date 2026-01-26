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
package com.github.tomakehurst.wiremock.http.trafficlistener;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
import java.nio.charset.Charset;

public final class WiremockNetworkTrafficListeners {
  private static final ConsoleNotifier CONSOLE_NOTIFIER = new ConsoleNotifier(true);

  private WiremockNetworkTrafficListeners() {}

  public static WiremockNetworkTrafficListener createNotifying(Notifier notifier, Charset charset) {
    return new NotifyingWiremockNetworkTrafficListener(notifier, charset);
  }

  public static WiremockNetworkTrafficListener createConsoleNotifying() {
    return new NotifyingWiremockNetworkTrafficListener(CONSOLE_NOTIFIER, UTF_8);
  }

  public static WiremockNetworkTrafficListener createConsoleNotifying(Charset charset) {
    return new NotifyingWiremockNetworkTrafficListener(CONSOLE_NOTIFIER, charset);
  }
}
