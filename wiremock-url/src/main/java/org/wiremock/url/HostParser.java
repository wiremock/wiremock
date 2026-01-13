/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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

import static org.wiremock.url.Constants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class HostParser implements PercentEncodedStringParser<Host> {

  static final HostParser INSTANCE = new HostParser();

  static final String ipv6Address = "(?<ipv6Address>[0-9A-Fa-f:.]+)";
  static final String ipvFuture = "v[0-9A-Fa-f]\\.[" + unreserved + subDelims + ":]+";
  static final String ipLiteral = "\\[(?:" + ipv6Address + "|" + ipvFuture + ")]";
  static final String registeredName = "(?:[" + unreserved + subDelims + "]|" + pctEncoded + ")*";
  // all ipv4 addresses are also legal registered names
  static final String hostRegex = ipLiteral + "|" + registeredName;

  private final Pattern hostPattern = Pattern.compile("^" + hostRegex + "$");

  @Override
  public Host parse(String stringForm) throws IllegalHost {
    if (stringForm.isEmpty()) return Host.EMPTY;
    Matcher matcher = hostPattern.matcher(stringForm);
    if (matcher.matches()) {
      String ipv6Address = matcher.group("ipv6Address");
      if (ipv6Address != null) {
        if (!ipv6Address.contains(":")) {
          throw new IllegalHost(stringForm);
        }
        try {
          //noinspection ResultOfMethodCallIgnored
          InetAddress.getByName(ipv6Address);
        } catch (UnknownHostException e) {
          throw new IllegalHost(stringForm);
        }
      }
      return new HostValue(stringForm);
    } else {
      throw new IllegalHost(stringForm);
    }
  }

  private static final boolean[] hostCharSet = combine(unreservedCharSet, subDelimCharSet);

  @Override
  public Host encode(String unencoded) {
    var result = Constants.encode(unencoded, hostCharSet);
    if (unencoded.isEmpty()) {
      return Host.EMPTY;
    } else {
      return new HostValue(result, true);
    }
  }
}
