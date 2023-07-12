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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Stopwatch;

public class Timing {

  public static final Timing UNTIMED = create();

  private volatile Integer addedDelay;
  private volatile Integer processTime;
  private volatile Integer responseSendTime;

  public static Timing create() {
    return new Timing(null, null, null, null, null);
  }

  private Timing(
      @JsonProperty("addedDelay") Integer addedDelay,
      @JsonProperty("processTime") Integer processTime,
      @JsonProperty("responseSendTime") Integer responseSendTime,
      @JsonProperty("serveTime") Integer ignored1,
      @JsonProperty("totalTime") Integer ignored2) {
    this.addedDelay = addedDelay;
    this.processTime = processTime;
    this.responseSendTime = responseSendTime;
  }

  /** The delay added to the response via the stub or global configuration */
  public int getAddedDelay() {
    return addedDelay;
  }

  /** The amount of time spent handling the stub request */
  public int getProcessTime() {
    return processTime;
  }

  /** The amount of time taken to send the response to the client */
  public int getResponseSendTime() {
    return responseSendTime;
  }

  /** The total request time from start to finish, minus added delay */
  public int getServeTime() {
    return processTime + responseSendTime;
  }

  /** The total request time including added delay */
  public int getTotalTime() {
    return getServeTime() + addedDelay;
  }

  public void setAddedTime(int addedDelayMillis) {
    this.addedDelay = addedDelayMillis;
  }

  public void logProcessTime(Stopwatch stopwatch) {
    processTime = (int) stopwatch.elapsed(MILLISECONDS);
  }

  public void logResponseSendTime(Stopwatch stopwatch) {
    responseSendTime = (int) stopwatch.elapsed(MILLISECONDS);
  }
}
