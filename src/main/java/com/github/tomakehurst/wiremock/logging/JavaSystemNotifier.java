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
package com.github.tomakehurst.wiremock.logging;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

import com.github.tomakehurst.wiremock.common.Notifier;

public class JavaSystemNotifier implements Notifier {

  private final System.Logger log = System.getLogger("WireMock");

  private final boolean verbose;

  public JavaSystemNotifier(boolean verbose) {
    this.verbose = verbose;
  }

  @Override
  public void info(String message) {
    if (verbose) {
      log.log(INFO, message);
    }
  }

  @Override
  public void error(String message) {
    log.log(ERROR, message);
  }

  @Override
  public void error(String message, Throwable t) {
    log.log(ERROR, message, t);
  }
}
