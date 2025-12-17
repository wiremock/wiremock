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

import java.net.URLDecoder;

public interface Segment extends PercentEncoded {
  Segment EMPTY = new SegmentImpl("");
  Segment DOT = new SegmentImpl(".");
  Segment DOT_DOT = new SegmentImpl("..");

  default boolean isDot() {
    return decode().equals(Segment.DOT.toString());
  }

  default boolean isDotDot() {
    return decode().equals(Segment.DOT_DOT.toString());
  }
}

record SegmentImpl(String stringForm) implements Segment {

  @Override
  public String decode() {
    try {
      return URLDecoder.decode(stringForm, UTF_8);
    } catch (IllegalArgumentException ignored) {
      return stringForm;
    }
  }

  @Override
  public int length() {
    return stringForm.length();
  }

  @Override
  public char charAt(int index) {
    return stringForm.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return stringForm.subSequence(start, end);
  }

  @Override
  public String toString() {
    return stringForm;
  }
}
