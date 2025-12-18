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
import static org.wiremock.url.Constants.pctEncodedPattern;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Host {

  Host normalise();

  static Host parse(String hostString) throws IllegalHost {
    return HostParser.INSTANCE.parse(hostString);
  }
}

class HostParser implements CharSequenceParser<Host> {

  static final HostParser INSTANCE = new HostParser();

  final String ipv6Address = "(?<ipv6Address>[0-9A-Fa-f:.]+)";
  final String ipvFuture = "v[0-9A-Fa-f]\\.[" + unreserved + subDelims + ":]+";
  final String ipLiteral = "\\[(?:" + ipv6Address + "|" + ipvFuture + ")]";
  final String registeredName = "(?:[" + unreserved + subDelims + "]|" + pctEncoded + ")*";
  // all ipv4 addresses are also legal registered names
  final String hostRegex = ipLiteral + "|" + registeredName;

  private final Pattern hostPattern = Pattern.compile("^" + hostRegex + "$");

  @Override
  public Host parse(CharSequence stringForm) throws IllegalHost {
    String hostStr = stringForm.toString();
    Matcher matcher = hostPattern.matcher(hostStr);
    if (matcher.matches()) {
      String ipv6Address = matcher.group("ipv6Address");
      if (ipv6Address != null) {
        if (!ipv6Address.contains(":")) {
          throw new IllegalHost(hostStr);
        }
        try {
          //noinspection ResultOfMethodCallIgnored
          InetAddress.getByName(ipv6Address);
        } catch (UnknownHostException e) {
          throw new IllegalHost(hostStr);
        }
      }
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

    @Override
    public org.wiremock.url.Host normalise() {
      StringBuilder result = new StringBuilder();
      Matcher matcher = pctEncodedPattern.matcher(host);
      int lastEnd = 0;

      while (matcher.find()) {
        // Lowercase the part before the percent-encoded sequence
        result.append(host.substring(lastEnd, matcher.start()).toLowerCase(Locale.ROOT));
        // Uppercase the percent-encoded sequence
        result.append(matcher.group().toUpperCase(Locale.ROOT));
        lastEnd = matcher.end();
      }

      // Lowercase the remaining part
      result.append(host.substring(lastEnd).toLowerCase(Locale.ROOT));

      String normalised = result.toString();
      if (normalised.equals(host)) {
        return this;
      } else {
        return new Host(normalised);
      }
    }
  }
}
