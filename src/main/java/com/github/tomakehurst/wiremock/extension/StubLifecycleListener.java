/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/** The interface Stub lifecycle listener. */
public interface StubLifecycleListener extends Extension {

  /**
   * Before stub created.
   *
   * @param stub the stub
   */
  default void beforeStubCreated(StubMapping stub) {}

  /**
   * After stub created.
   *
   * @param stub the stub
   */
  default void afterStubCreated(StubMapping stub) {}

  /**
   * Before stub edited.
   *
   * @param oldStub the old stub
   * @param newStub the new stub
   */
  default void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {}

  /**
   * After stub edited.
   *
   * @param oldStub the old stub
   * @param newStub the new stub
   */
  default void afterStubEdited(StubMapping oldStub, StubMapping newStub) {}

  /**
   * Before stub removed.
   *
   * @param stub the stub
   */
  default void beforeStubRemoved(StubMapping stub) {}

  /**
   * After stub removed.
   *
   * @param stub the stub
   */
  default void afterStubRemoved(StubMapping stub) {}

  /** Before stubs reset. */
  default void beforeStubsReset() {}

  /** After stubs reset. */
  default void afterStubsReset() {}
}
