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

public interface Segment extends PercentEncoded {
  Segment EMPTY = new SegmentImpl("");
  Segment DOT = new SegmentImpl(".");
  Segment DOT_DOT = new SegmentImpl("..");
}

record SegmentImpl(String stringForm) implements Segment {
  @Override
  public String toString() {
    return stringForm;
  }
}
