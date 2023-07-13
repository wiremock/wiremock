/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/** @deprecated Use {@link ServeEventListener} instead. */
@Deprecated
public abstract class PostServeAction implements Extension {

  /**
   * Do something after a request has been served. Called when this extension is applied to a
   * specific stub mapping.
   *
   * @param serveEvent the serve event, including the request and the response definition
   * @param admin WireMock's admin functions
   * @param parameters the parameters passed to the extension from the stub mapping
   */
  public void doAction(ServeEvent serveEvent, Admin admin, Parameters parameters) {};

  /**
   * Do something after a request has been served. Called when this extension is applied to a
   * specific stub mapping.
   *
   * @param serveEvent the serve event, including the request and the response definition
   * @param admin WireMock's admin functions
   */
  public void doGlobalAction(ServeEvent serveEvent, Admin admin) {};
}
