/*
 * Copyright (C) 2014-2023 Thomas Akehurst
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

import java.util.Optional;

/**
 * Exposed Jetty tuning options. See: <a
 * href="https://www.eclipse.org/jetty/javadoc/jetty-11/org/eclipse/jetty/server/AbstractConnector.html">AbstractConnector</a>
 */
public class JettySettings {
  private final Integer acceptors;
  private final Integer acceptQueueSize;
  private final Integer requestHeaderSize;
  private final Integer responseHeaderSize;
  private final Long stopTimeout;
  private final Long idleTimeout;
  private final Long shutdownIdleTimeout;

  private JettySettings(
      Integer acceptors,
      Integer acceptQueueSize,
      Integer requestHeaderSize,
      Integer responseHeaderSize,
      Long stopTimeout,
      Long idleTimeout,
      Long shutdownIdleTimeout) {
    this.acceptors = acceptors;
    this.acceptQueueSize = acceptQueueSize;
    this.requestHeaderSize = requestHeaderSize;
    this.responseHeaderSize = responseHeaderSize;
    this.stopTimeout = stopTimeout;
    this.idleTimeout = idleTimeout;
    this.shutdownIdleTimeout = shutdownIdleTimeout;
  }

  public Optional<Integer> getAcceptors() {
    return Optional.ofNullable(acceptors);
  }

  public Optional<Integer> getAcceptQueueSize() {
    return Optional.ofNullable(acceptQueueSize);
  }

  public Optional<Integer> getRequestHeaderSize() {
    return Optional.ofNullable(requestHeaderSize);
  }

  public Optional<Integer> getResponseHeaderSize() {
    return Optional.ofNullable(responseHeaderSize);
  }

  public Optional<Long> getStopTimeout() {
    return Optional.ofNullable(stopTimeout);
  }

  public Optional<Long> getIdleTimeout() {
    return Optional.ofNullable(idleTimeout);
  }

  public Optional<Long> getShutdownIdleTimeout() {
    return Optional.ofNullable(shutdownIdleTimeout);
  }

  @Override
  public String toString() {
    return "JettySettings{"
        + "acceptors="
        + acceptors
        + ", acceptQueueSize="
        + acceptQueueSize
        + ", requestHeaderSize="
        + requestHeaderSize
        + ", responseHeaderSize="
        + responseHeaderSize
        + '}';
  }

  public static class Builder {
    private Integer acceptors;
    private Integer acceptQueueSize;
    private Integer requestHeaderSize;
    private Integer responseHeaderSize;
    private Long stopTimeout;
    private Long idleTimeout;
    private Long shutdownIdleTimeout;

    private Builder() {}

    public static Builder aJettySettings() {
      return new Builder();
    }

    public Builder withAcceptors(Integer acceptors) {
      this.acceptors = acceptors;
      return this;
    }

    public Builder withAcceptQueueSize(Integer acceptQueueSize) {
      this.acceptQueueSize = acceptQueueSize;
      return this;
    }

    public Builder withRequestHeaderSize(Integer requestHeaderSize) {
      this.requestHeaderSize = requestHeaderSize;
      return this;
    }

    public Builder withResponseHeaderSize(Integer responseHeaderSize) {
      this.responseHeaderSize = responseHeaderSize;
      return this;
    }

    public Builder withStopTimeout(Long stopTimeout) {
      this.stopTimeout = stopTimeout;
      return this;
    }

    public Builder withIdleTimeout(Long idleTimeout) {
      this.idleTimeout = idleTimeout;
      return this;
    }

    public Builder withShutdownIdleTimeout(Long shutdownIdleTimeout) {
      this.shutdownIdleTimeout = shutdownIdleTimeout;
      return this;
    }

    public JettySettings build() {
      return new JettySettings(
          acceptors,
          acceptQueueSize,
          requestHeaderSize,
          responseHeaderSize,
          stopTimeout,
          idleTimeout,
          shutdownIdleTimeout);
    }
  }
}
