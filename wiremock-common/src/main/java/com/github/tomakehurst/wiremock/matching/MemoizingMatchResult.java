/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.List;

public class MemoizingMatchResult extends MatchResult {

  private final Supplier<Double> memoizedDistance =
      Suppliers.memoize(
          new Supplier<>() {
            @Override
            public Double get() {
              return target.getDistance();
            }
          });

  private final Supplier<Boolean> memoizedExactMatch =
      Suppliers.memoize(
          new Supplier<>() {
            @Override
            public Boolean get() {
              return target.isExactMatch();
            }
          });

  private final MatchResult target;

  public MemoizingMatchResult(MatchResult target) {
    this.target = target;
  }

  @Override
  public boolean isExactMatch() {
    return memoizedExactMatch.get();
  }

  @Override
  public double getDistance() {
    return memoizedDistance.get();
  }

  @Override
  public List<SubEvent> getSubEvents() {
    return target.getSubEvents();
  }

  @Override
  public List<DiffDescription> getDiffDescriptions() {
    return target.getDiffDescriptions();
  }
}
