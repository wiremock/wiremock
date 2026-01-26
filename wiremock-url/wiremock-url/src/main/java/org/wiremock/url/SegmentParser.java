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

import static org.wiremock.url.Constants.alwaysIllegal;
import static org.wiremock.url.Constants.combine;
import static org.wiremock.url.Constants.include;
import static org.wiremock.url.Constants.subDelimCharSet;
import static org.wiremock.url.Constants.unreservedCharSet;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class SegmentParser implements PercentEncodedStringParser<Segment> {

  public static final SegmentParser INSTANCE = new SegmentParser();

  private static final String segmentRegex = "[^#?/" + alwaysIllegal + "]*";
  private static final Pattern segmentPattern = Pattern.compile("^" + segmentRegex + "$");

  @Override
  public Class<Segment> getType() {
    return Segment.class;
  }

  @Override
  public Segment parse(String stringForm) {
    return build(stringForm, () -> buildSafely(stringForm));
  }

  private Segment buildSafely(String stringForm) {
    if (segmentPattern.matcher(stringForm).matches()) {
      return new SegmentValue(stringForm);
    } else {
      throw new IllegalSegment(stringForm);
    }
  }

  static final boolean[] segmentCharSet =
      combine(unreservedCharSet, subDelimCharSet, include(':', '@'));

  @Override
  public Segment encode(String unencoded) {
    return build(
        unencoded, () -> new SegmentValue(Constants.encode(unencoded, segmentCharSet), true));
  }

  Segment build(String stringForm, Supplier<Segment> builder) {
    return switch (stringForm) {
      case "" -> Segment.EMPTY;
      case "." -> Segment.DOT;
      case ".." -> Segment.DOT_DOT;
      default -> builder.get();
    };
  }
}
