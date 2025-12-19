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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static org.wiremock.url.Constants.pctEncoded;
import static org.wiremock.url.Constants.pctEncodedPattern;
import static org.wiremock.url.Constants.subDelims;
import static org.wiremock.url.Constants.unreserved;
import static org.wiremock.url.Strings.transform;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Host extends PercentEncoded {

  Host normalise();

  static Host parse(String hostString) throws IllegalHost {
    return HostParser.INSTANCE.parse(hostString);
  }

  static Host encode(String unencoded) {
    return HostParser.INSTANCE.encode(unencoded);
  }
}

class HostParser implements PercentEncodedCharSequenceParser<Host> {

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

  @Override
  public Host encode(String unencoded) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < unencoded.length(); i++) {
      char c = unencoded.charAt(i);
      if (isUnreserved(c) || isSubDelim(c)) {
        result.append(c);
      } else {
        byte[] bytes = String.valueOf(c).getBytes(UTF_8);
        for (byte b : bytes) {
          result.append('%');
          result.append(String.format("%02X", b & 0xFF));
        }
      }
    }
    return new Host(result.toString());
  }

  private boolean isUnreserved(char c) {
    return (c >= 'A' && c <= 'Z')
        || (c >= 'a' && c <= 'z')
        || (c >= '0' && c <= '9')
        || c == '-'
        || c == '.'
        || c == '_'
        || c == '~';
  }

  private boolean isSubDelim(char c) {
    return c == '!' || c == '$' || c == '&' || c == '\'' || c == '(' || c == ')' || c == '*'
        || c == '+' || c == ',' || c == ';' || c == '=';
  }

  record Host(String host) implements org.wiremock.url.Host {
    @Override
    public String toString() {
      return host;
    }

    @Override
    public org.wiremock.url.Host normalise() {

      String normalised =
          transform(
              host,
              pctEncodedPattern,
              matched -> matched.toUpperCase(ROOT),
              unmatched -> unmatched.toLowerCase(ROOT));
      if (normalised.equals(host)) {
        return this;
      } else {
        return new Host(normalised);
      }
    }
  }
}
