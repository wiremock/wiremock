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
package org.wiremock.url;

import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;

import java.util.regex.Pattern;

public interface Host {

  static Host parse(String hostString) throws IllegalHost {
    return HostParser.INSTANCE.parse(hostString);
  }
}

class HostParser implements CharSequenceParser<Host> {

  static final HostParser INSTANCE = new HostParser();

  final String octet = "(([1-2][0-9][0-9])|([0-9][0-9])|([0-9]))";
  final String ipv4Address = "(?<ipv4Address>" + octet + "(\\." + octet + "){3})";
  final String ipV6Address = "(\\[(?<ipV6Address>[^]]+)])";
  final String registeredName = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + ")*";
  final String hostRegex =
      "(" + ipV6Address + "|" + ipv4Address + "|(?<registeredName>" + registeredName + "))";

  private final Pattern hostPattern = Pattern.compile("^" + hostRegex + "$");

  @Override
  public Host parse(CharSequence stringForm) throws IllegalHost {
    String hostStr = stringForm.toString();
    if (hostPattern.matcher(hostStr).matches()) {
      return new Host(hostStr);
    } else {
      throw new IllegalHost(hostStr);
    }
  }

  record Host(String host) implements org.wiremock.url.Host {
    @Override
    public String toString() {
      return host;
    }
  }
}
