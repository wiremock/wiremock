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

import com.github.tomakehurst.wiremock.global.RequestDelayControl;
import org.mortbay.jetty.bio.SocketConnector;

import java.io.IOException;
import java.net.Socket;

class DelayableSocketConnector extends SocketConnector {

    private final RequestDelayControl requestDelayControl;

    DelayableSocketConnector(RequestDelayControl requestDelayControl) {
        this.requestDelayControl = requestDelayControl;
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
        final Socket socket = _serverSocket.accept();

        try {
            requestDelayControl.delayIfRequired();
        } catch (InterruptedException e) {
            if (!(isStopping() || isStopped())) {
                Thread.interrupted(); // Clear the interrupt flag on the current thread
            }
        }

        configure(socket);
        Connection connection = new Connection(socket) {
            @Override
            public void run() {
                ActiveSocket.set(socket);
                super.run();
                ActiveSocket.clear();
            }
        };
        connection.dispatch();
    }
}
