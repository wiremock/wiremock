/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.core.FaultInjector;
import com.github.tomakehurst.wiremock.servlet.FaultInjectorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JettyFaultInjectorFactory implements FaultInjectorFactory {
  private final JettyHttpUtils utils;

  public JettyFaultInjectorFactory(JettyHttpUtils utils) {
    this.utils = utils;
  }

  @Override
  public FaultInjector buildFaultInjector(
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    if (httpServletRequest.getScheme().equals("https")) {
      return new JettyHttpsFaultInjector(httpServletResponse, utils);
    }

    return new JettyFaultInjector(httpServletResponse, utils);
  }
}
