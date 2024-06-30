/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.List;

public class EagerMatchResult extends MatchResult {

  private final double distance;

  EagerMatchResult(double distance) {
    this(distance, List.of());
  }

  EagerMatchResult(double distance, List<SubEvent> subEvents) {
    this(distance, subEvents, List.of());
  }

  public EagerMatchResult(
      double distance, List<SubEvent> subEvents, List<DiffDescription> diffDescriptions) {
    super(subEvents, diffDescriptions);
    this.distance = distance;
  }

  public double getDistance() {
    return distance;
  }

  public boolean isExactMatch() {
    return distance == 0;
  }
}
