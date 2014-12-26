/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty6;

import com.github.tomakehurst.wiremock.core.FaultInjector;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class Jetty6FaultInjector implements FaultInjector {

    private final HttpServletResponse response;

    public Jetty6FaultInjector(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void emptyResponseAndCloseConnection() {
        try {
            ActiveSocket.get().close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void malformedResponseChunk() {
        Socket socket = ActiveSocket.get();
        try {
            response.setStatus(200);
            response.flushBuffer();
            socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }

    @Override
    public void randomDataAndCloseConnection() {
        Socket socket = ActiveSocket.get();
        try {
            socket.getOutputStream().write("lskdu018973t09sylgasjkfg1][]'./.sdlv".getBytes());
            socket.close();
        } catch (IOException e) {
            throwUnchecked(e);
        }
    }
}
