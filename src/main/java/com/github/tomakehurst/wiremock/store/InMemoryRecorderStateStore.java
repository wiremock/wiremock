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

import com.github.tomakehurst.wiremock.recording.RecorderState;
import org.wiremock.annotations.Beta;

import java.util.concurrent.atomic.AtomicReference;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryRecorderStateStore implements RecorderStateStore {

  private final AtomicReference<RecorderState> store;

  public InMemoryRecorderStateStore() {
    this.store = new AtomicReference<>(RecorderState.initial());
  }

  @Override
  public RecorderState get() {
    return store.get();
  }

  @Override
  public void set(RecorderState state) {
    store.set(state);
  }
}
