/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class SingleConnectionServer {

  private final Thread thread;
  private final ServerSocket serverSocket;

  public SingleConnectionServer() throws IOException {
    this.serverSocket = new ServerSocket(0);
    this.thread =
        new Thread(
            () -> {
              Socket socket = null;
              try {
                socket = serverSocket.accept();
                socket.setSoTimeout(500);
                handleClientConnection(socket);
                serverSocket.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              } finally {
                try {
                  serverSocket.close();
                  if (socket != null) {
                    socket.close();
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
  }

  public void start() {
    thread.start();
  }

  public void stop() throws InterruptedException {
    thread.interrupt();
    thread.join();
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  private void handleClientConnection(Socket clientSocket) throws IOException {
    BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

    String requestLine;
    try {
      while ((requestLine = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
        if (requestLine.startsWith("POST /admin-test/__admin/reset")) {
          String line;
          while (!(line = reader.readLine()).isBlank()) {
            // Discard headers
          }

          // Send the response with status 200 OK
          String response = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n";
          writer.println(response);
        }
      }
    } catch (SocketTimeoutException e) {
      // Ignore
    }

    clientSocket.close();
  }
}
