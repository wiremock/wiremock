/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.core.FaultInjector;

/** The enum Fault. */
public enum Fault {
  /** The Connection reset by peer. */
  CONNECTION_RESET_BY_PEER {
    @Override
    public void apply(FaultInjector faultInjector) {
      faultInjector.connectionResetByPeer();
    }
  },

  /** The Empty response. */
  EMPTY_RESPONSE {
    @Override
    public void apply(FaultInjector faultInjector) {
      faultInjector.emptyResponseAndCloseConnection();
    }
  },

  /** The Malformed response chunk. */
  MALFORMED_RESPONSE_CHUNK {
    @Override
    public void apply(FaultInjector faultInjector) {
      faultInjector.malformedResponseChunk();
    }
  },

  /** The Random data then close. */
  RANDOM_DATA_THEN_CLOSE {
    @Override
    public void apply(FaultInjector faultInjector) {
      faultInjector.randomDataAndCloseConnection();
    }
  };

  /**
   * Apply.
   *
   * @param faultInjector the fault injector
   */
  public abstract void apply(FaultInjector faultInjector);
}
