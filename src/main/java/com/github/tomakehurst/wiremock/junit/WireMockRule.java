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
package com.github.tomakehurst.wiremock.junit;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/** The type Wire mock rule. */
public class WireMockRule extends WireMockServer implements TestRule {

  private final boolean failOnUnmatchedRequests;

  /**
   * Instantiates a new Wire mock rule.
   *
   * @param options the options
   */
  public WireMockRule(Options options) {
    this(options, true);
  }

  /**
   * Instantiates a new Wire mock rule.
   *
   * @param options the options
   * @param failOnUnmatchedRequests the fail on unmatched requests
   */
  public WireMockRule(Options options, boolean failOnUnmatchedRequests) {
    super(options);
    this.failOnUnmatchedRequests = failOnUnmatchedRequests;
  }

  /**
   * Instantiates a new Wire mock rule.
   *
   * @param port the port
   */
  public WireMockRule(int port) {
    this(wireMockConfig().port(port));
  }

  /**
   * Instantiates a new Wire mock rule.
   *
   * @param port the port
   * @param httpsPort the https port
   */
  public WireMockRule(int port, Integer httpsPort) {
    this(wireMockConfig().port(port).httpsPort(httpsPort));
  }

  /** Instantiates a new Wire mock rule. */
  public WireMockRule() {
    this(wireMockConfig());
  }

  /**
   * Apply statement.
   *
   * @param base the base
   * @param description the description
   * @return the statement
   */
  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        start();

        if (options.getHttpDisabled()) {
          WireMock.configureFor("https", "localhost", httpsPort());
        } else {
          WireMock.configureFor("localhost", port());
        }

        try {
          before();
          base.evaluate();

          if (failOnUnmatchedRequests) {
            checkForUnmatchedRequests();
          }
        } finally {
          after();
          stop();
        }
      }
    };
  }

  /** Before. */
  protected void before() {
    // NOOP
  }

  /** After. */
  protected void after() {
    // NOOP
  }
}
