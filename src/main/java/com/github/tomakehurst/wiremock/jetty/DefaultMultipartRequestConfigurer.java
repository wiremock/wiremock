/*
 * Copyright (C) 2019-2022 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServletRequest;

public class DefaultMultipartRequestConfigurer implements MultipartRequestConfigurer {

  @Override
  public void configure(HttpServletRequest request) {
    request.setAttribute(
        org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT,
        new MultipartConfigElement(
            System.getProperty("java.io.tmpdir"), Integer.MAX_VALUE, -1L, 0));
  }
}
