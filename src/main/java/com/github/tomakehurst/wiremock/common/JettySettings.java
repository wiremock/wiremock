/*
 * Copyright (C) 2014-2025 Thomas Akehurst
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

  /**
   * Gets acceptors.
   *
   * @return the acceptors
   */
  public Optional<Integer> getAcceptors() {
    return Optional.ofNullable(acceptors);
  }

  /**
   * Gets accept queue size.
   *
   * @return the accept queue size
   */
  public Optional<Integer> getAcceptQueueSize() {
    return Optional.ofNullable(acceptQueueSize);
  }

  /**
   * Gets request header size.
   *
   * @return the request header size
   */
  public Optional<Integer> getRequestHeaderSize() {
    return Optional.ofNullable(requestHeaderSize);
  }

  /**
   * Gets response header size.
   *
   * @return the response header size
   */
  public Optional<Integer> getResponseHeaderSize() {
    return Optional.ofNullable(responseHeaderSize);
  }

  /**
   * Gets stop timeout.
   *
   * @return the stop timeout
   */
  public Optional<Long> getStopTimeout() {
    return Optional.ofNullable(stopTimeout);
  }

  /**
   * Gets idle timeout.
   *
   * @return the idle timeout
   */
  public Optional<Long> getIdleTimeout() {
    return Optional.ofNullable(idleTimeout);
  }

  /**
   * Gets shutdown idle timeout.
   *
   * @return the shutdown idle timeout
   */
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

  /** The type Builder. */
  public static class Builder {
    private Integer acceptors;
    private Integer acceptQueueSize;
    private Integer requestHeaderSize;
    private Integer responseHeaderSize;
    private Long stopTimeout;
    private Long idleTimeout;

    private Builder() {}

    /**
     * A jetty settings builder.
     *
     * @return the builder
     */
    public static Builder ajettysettings() {
      return new Builder();
    }

    /**
     * With acceptors builder.
     *
     * @param acceptors the acceptors
     * @return the builder
     */
    public Builder withAcceptors(Integer acceptors) {
      this.acceptors = acceptors;
      return this;
    }

    /**
     * With accept queue size builder.
     *
     * @param acceptQueueSize the accept queue size
     * @return the builder
     */
    public Builder withAcceptQueueSize(Integer acceptQueueSize) {
      this.acceptQueueSize = acceptQueueSize;
      return this;
    }

    /**
     * With request header size builder.
     *
     * @param requestHeaderSize the request header size
     * @return the builder
     */
    public Builder withRequestHeaderSize(Integer requestHeaderSize) {
      this.requestHeaderSize = requestHeaderSize;
      return this;
    }

    /**
     * With response header size builder.
     *
     * @param responseHeaderSize the response header size
     * @return the builder
     */
    public Builder withResponseHeaderSize(Integer responseHeaderSize) {
      this.responseHeaderSize = responseHeaderSize;
      return this;
    }

    /**
     * With stop timeout builder.
     *
     * @param stopTimeout the stop timeout
     * @return the builder
     */
    public Builder withStopTimeout(Long stopTimeout) {
      this.stopTimeout = stopTimeout;
      return this;
    }

    /**
     * With idle timeout builder.
     *
     * @param idleTimeout the idle timeout
     * @return the builder
     */
    public Builder withIdleTimeout(Long idleTimeout) {
      this.idleTimeout = idleTimeout;
      return this;
    }

    public JettySettings build() {
        return null;
    }

    /**
     * Build jetty settings.
     *
     * @return the jetty settings
     */
    /*
    public JettySettings build() {
      return new JettySettings(
          acceptors,
          acceptQueueSize,
          requestHeaderSize,
          responseHeaderSize,
          stopTimeout,
          idleTimeout,
          shutdownIdleTimeout);
    }*/
  }
}
