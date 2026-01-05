/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.jetty;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProhibitedNetworkAddressException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.SocketAddressResolver;

/**
 * A Jetty SocketAddressResolver that enforces network address rules. This prevents proxying to
 * denied IP addresses or hostnames.
 */
class NetworkAddressRulesAdheringSocketAddressResolver implements SocketAddressResolver {

  private final NetworkAddressRules networkAddressRules;

  public NetworkAddressRulesAdheringSocketAddressResolver(NetworkAddressRules networkAddressRules) {
    this.networkAddressRules = networkAddressRules;
  }

  @Override
  public void resolve(
      String host,
      int port,
      Map<String, Object> context,
      Promise<List<InetSocketAddress>> promise) {
    // First check if the hostname itself is allowed
    if (!networkAddressRules.isAllowed(host)) {
      promise.failed(new ProhibitedNetworkAddressException());
      return;
    }

    try {
      // Perform DNS resolution
      InetAddress[] addresses = InetAddress.getAllByName(host);
      List<InetSocketAddress> socketAddresses = new ArrayList<>();

      // Check each resolved IP address
      for (InetAddress address : addresses) {
        String ipAddress = address.getHostAddress();

        // If not all addresses are allowed, check each one
        if (!networkAddressRules.isAllowedAll() && !networkAddressRules.isAllowed(ipAddress)) {
          promise.failed(new ProhibitedNetworkAddressException());
          return;
        }

        socketAddresses.add(new InetSocketAddress(address, port));
      }

      // All addresses are allowed
      promise.succeeded(socketAddresses);
    } catch (UnknownHostException e) {
      promise.failed(e);
    }
  }
}
