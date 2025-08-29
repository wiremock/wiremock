/*
 * Copyright (C) 2021-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/** The interface Wire mock test. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(WireMockExtension.class)
public @interface WireMockTest {

  /**
   * Extension scanning enabled boolean.
   *
   * @return the boolean
   */
  boolean extensionScanningEnabled() default false;

  /**
   * Http port int.
   *
   * @return the int
   */
  int httpPort() default 0;

  /**
   * Https enabled boolean.
   *
   * @return the boolean
   */
  boolean httpsEnabled() default false;

  /**
   * Https port int.
   *
   * @return the int
   */
  int httpsPort() default 0;

  /**
   * Proxy mode boolean.
   *
   * @return the boolean
   */
  boolean proxyMode() default false;
}
