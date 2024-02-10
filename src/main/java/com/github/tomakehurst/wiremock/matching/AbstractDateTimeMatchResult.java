/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

public abstract class AbstractDateTimeMatchResult extends MatchResult {

  private static final long ONE_YEAR_IN_MILLIS = 365 * 24 * 60 * 60 * 1000L;

  private final boolean isZoned;
  private final boolean isLocal;

  protected final LocalDateTime localExpected;
  protected final ZonedDateTime zonedExpected;

  protected final ZonedDateTime zonedActual;
  protected final LocalDateTime localActual;

  public AbstractDateTimeMatchResult(
      ZonedDateTime zonedExpected,
      LocalDateTime localExpected,
      ZonedDateTime zonedActual,
      LocalDateTime localActual) {
    this.zonedExpected = zonedExpected;
    this.localExpected = localExpected;
    this.isZoned = zonedExpected != null;
    this.isLocal = localExpected != null;
    this.zonedActual = zonedActual;
    this.localActual = localActual;
  }

  @Override
  public boolean isExactMatch() {
    boolean isMatch = false;
    if (isZoned && zonedActual != null) {
      isMatch = matchZonedZoned();
    } else if (isLocal && localActual != null) {
      isMatch = matchLocalLocal();
    } else if (isLocal && zonedActual != null) {
      isMatch = matchLocalZoned();
    } else if (isZoned && localActual != null) {
      isMatch = matchZonedLocal();
    }

    return isMatch;
  }

  protected abstract boolean matchZonedZoned();

  protected abstract boolean matchLocalLocal();

  protected abstract boolean matchLocalZoned();

  protected abstract boolean matchZonedLocal();

  @Override
  public double getDistance() {
    double distance = 1;
    if (isZoned && zonedActual != null) {
      distance = calculateDistance(zonedExpected, zonedActual);
    } else if (isLocal && localActual != null) {
      distance = calculateDistance(localExpected, localActual);
    } else if (isLocal && zonedActual != null) {
      distance = calculateDistance(localExpected, zonedActual.toLocalDateTime());
    }

    return distance;
  }

  private double calculateDistance(Temporal start, Temporal end) {
    long absoluteTimeDifference = Math.abs((ChronoUnit.MILLIS.between(start, end)));
    return (double) absoluteTimeDifference / (absoluteTimeDifference + 2 * ONE_YEAR_IN_MILLIS);
  }
}
