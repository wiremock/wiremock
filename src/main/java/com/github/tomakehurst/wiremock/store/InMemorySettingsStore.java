/*
 * Copyright (C) 2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.global.GlobalSettings;
import java.util.concurrent.atomic.AtomicReference;

public class InMemorySettingsStore implements SettingsStore {

  private final AtomicReference<GlobalSettings> holder;


  public InMemorySettingsStore(final Boolean proxyPassThrough) {
    holder = new AtomicReference<>(
        GlobalSettings.builder().proxyPassThrough(proxyPassThrough).build());
  }

  public InMemorySettingsStore() {
    this(true);
  }

  @Override
  public GlobalSettings get() {
    return holder.get();
  }

  @Override
  public void set(GlobalSettings newSettings) {
    holder.set(newSettings);
  }
}
