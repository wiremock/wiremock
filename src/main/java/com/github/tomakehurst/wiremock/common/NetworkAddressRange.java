/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class NetworkAddressRange {

  public static final NetworkAddressRange ALL = new All();

  private static final Pattern SINGLE_IP =
      Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
  private static final Pattern IP_RANGE =
      Pattern.compile(SINGLE_IP.pattern() + "-" + SINGLE_IP.pattern());

  public static NetworkAddressRange of(String value) {

    if (value == null || value.isEmpty()) {
      throw new InvalidInputException(Errors.single(17, value + " is not a valid network address"));
    }

    if (SINGLE_IP.matcher(value).matches()) {
      return new SingleIp(value);
    }

    if (IP_RANGE.matcher(value).matches()) {
      return new IpRange(value);
    }

    return new DomainNameWildcard(value);
  }

  public abstract boolean isIncluded(String testValue);

  private static class SingleIp extends NetworkAddressRange {

    private final InetAddress inetAddress;

    private SingleIp(String ipAddress) {
      this.inetAddress = parseIpAddress(ipAddress);
    }

    @Override
    public boolean isIncluded(String testValue) {
      return lookup(testValue).equals(inetAddress);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SingleIp singleIp = (SingleIp) o;
      return inetAddress.equals(singleIp.inetAddress);
    }

    @Override
    public int hashCode() {
      return Objects.hash(inetAddress);
    }

    @Override
    public String toString() {
      return inetAddress.toString();
    }
  }

  private static class IpRange extends NetworkAddressRange {

    private final BigInteger start;
    private final BigInteger end;
    private final String asString;

    private IpRange(String ipRange) {
      String[] parts = ipRange.split("-");
      if (parts.length != 2) {
        throw new InvalidInputException(Errors.single(18, ipRange + " is not a valid IP range"));
      }
      this.start = InetAddresses.toBigInteger(parseIpAddress(parts[0]));
      this.end = InetAddresses.toBigInteger(parseIpAddress(parts[1]));
      this.asString = ipRange;
    }

    @Override
    public boolean isIncluded(String testValue) {
      InetAddress testValueAddress = lookup(testValue);
      BigInteger intVal = InetAddresses.toBigInteger(testValueAddress);
      return intVal.compareTo(start) >= 0 && intVal.compareTo(end) <= 0;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      IpRange ipRange = (IpRange) o;
      return start.equals(ipRange.start) && end.equals(ipRange.end);
    }

    @Override
    public int hashCode() {
      return Objects.hash(start, end);
    }

    @Override
    public String toString() {
      return asString;
    }
  }

  static class DomainNameWildcard extends NetworkAddressRange {

    private final Pattern namePattern;

    private DomainNameWildcard(String namePattern) {
      String nameRegex = namePattern.replace(".", "\\.").replace("*", ".+");
      this.namePattern = Pattern.compile(nameRegex);
    }

    @Override
    public boolean isIncluded(String testValue) {
      return namePattern.matcher(testValue).matches();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DomainNameWildcard that = (DomainNameWildcard) o;
      return namePattern.pattern().equals(that.namePattern.pattern());
    }

    @Override
    public int hashCode() {
      return Objects.hash(namePattern.pattern());
    }

    @Override
    public String toString() {
      return namePattern.pattern();
    }
  }

  private static class All extends NetworkAddressRange {

    @Override
    public boolean isIncluded(String testValue) {
      return true;
    }
  }

  private static InetAddress parseIpAddress(String ipAddress) {
    if (!InetAddresses.isInetAddress(ipAddress)) {
      throw new InvalidInputException(Errors.single(16, ipAddress + " is not a valid IP address"));
    }

    return lookup(ipAddress);
  }

  private static InetAddress lookup(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      throw new InvalidInputException(
          e, Errors.single(17, host + " is not a valid network address"));
    }
  }
}
