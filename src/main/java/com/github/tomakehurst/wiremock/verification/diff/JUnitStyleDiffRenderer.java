/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.common.Strings;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JUnitStyleDiffRenderer {

  public String render(Diff diff) {
    List<DiffLine<?>> lines = diff.getLines();

    String expected =
        Joiner.on("\n").join(lines.stream().map(EXPECTED).collect(Collectors.toList()));
    String actual = Joiner.on("\n").join(lines.stream().map(ACTUAL).collect(Collectors.toList()));

    return lines.isEmpty() ? "" : junitStyleDiffMessage(expected, actual);
  }

  public static String junitStyleDiffMessage(Object expected, Object actual) {
    return String.format(
        " expected:<\n%s> but was:<\n%s>",
        Strings.normaliseLineBreaks(expected.toString()),
        Strings.normaliseLineBreaks(actual.toString()));
  }

  private static Function<DiffLine<?>, Object> EXPECTED =
      line -> line.isForNonMatch() ? line.getPrintedPatternValue() : line.getActual();

  private static Function<DiffLine<?>, Object> ACTUAL = DiffLine::getActual;
}
