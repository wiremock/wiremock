/*
 * Copyright (C) 2014-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty11.Jetty11HttpServer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class JettyHttpServerFactory implements HttpServerFactory {
  private static final MethodHandle SERVER_CONSTRUCTOR = getServerConstructor();

  @SuppressWarnings("unchecked")
  private static MethodHandle getServerConstructor() {
    try {
      Class<? extends JettyHttpServer> serverClass =
          (Class<? extends JettyHttpServer>)
              Class.forName("com.github.tomakehurst.wiremock.jetty12.Jetty12HttpServer");
      return safelyGetConstructor(
          serverClass, Options.class, AdminRequestHandler.class, StubRequestHandler.class);
    } catch (ClassNotFoundException e) {
      return safelyGetConstructor(
          Jetty11HttpServer.class,
          Options.class,
          AdminRequestHandler.class,
          StubRequestHandler.class);
    }
  }

  private static MethodHandle safelyGetConstructor(Class<?> clazz, Class<?>... parameterTypes) {
    try {
      return MethodHandles.publicLookup()
          .findConstructor(clazz, MethodType.methodType(void.class, parameterTypes));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return Exceptions.throwUnchecked(e, MethodHandle.class);
    }
  }

  @Override
  public HttpServer buildHttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    try {
      return (HttpServer)
          SERVER_CONSTRUCTOR.invoke(options, adminRequestHandler, stubRequestHandler);
    } catch (WrongMethodTypeException | ClassCastException e) {
      return Exceptions.throwUnchecked(e, HttpServer.class);
    } catch (Throwable e) {
      if (e.getCause() != null) {
        return Exceptions.throwUnchecked(e.getCause(), null);
      } else {
        return Exceptions.throwUnchecked(e, null);
      }
    }
  }
}
