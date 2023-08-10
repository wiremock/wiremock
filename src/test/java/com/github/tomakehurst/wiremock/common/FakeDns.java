/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class FakeDns implements Dns {

  private final ConcurrentMap<String, InetAddress[]> names = new ConcurrentHashMap<>();

  public FakeDns register(String name, InetAddress... addresses) {
    names.put(name, addresses);
    return this;
  }

  @Override
  public InetAddress[] getAllByName(String host) throws UnknownHostException {
    if (InetAddresses.isInetAddress(host)) {
      return InetAddress.getAllByName(host);
    }
    InetAddress[] results = names.get(host);
    if (results == null || results.length == 0) {
      throw new UnknownHostException(host);
    }
    return results;
  }

  @Override
  public InetAddress getByName(String host) throws UnknownHostException {
    return getAllByName(host)[0];
  }
}
