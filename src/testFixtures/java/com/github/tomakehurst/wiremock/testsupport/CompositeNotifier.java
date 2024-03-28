/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.Notifier;
import java.util.Arrays;
import java.util.List;

public class CompositeNotifier implements Notifier {

  private final List<Notifier> notifiers;

  public CompositeNotifier(Notifier... notifiers) {
    this(Arrays.asList(notifiers));
  }

  public CompositeNotifier(List<Notifier> notifiers) {
    this.notifiers = notifiers;
  }

  @Override
  public void info(String message) {
    notifiers.forEach(notifier -> notifier.info(message));
  }

  @Override
  public void error(String message) {
    notifiers.forEach(notifier -> notifier.error(message));
  }

  @Override
  public void error(String message, Throwable t) {
    notifiers.forEach(notifier -> notifier.error(message, t));
  }
}
