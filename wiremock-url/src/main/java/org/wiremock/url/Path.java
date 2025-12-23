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

import java.util.List;

public interface Path extends PercentEncoded {

  Path EMPTY = PathParser.INSTANCE.parse("");
  Path ROOT = PathParser.INSTANCE.parse("/");

  boolean isAbsolute();

  List<Segment> segments();

  static Path parse(CharSequence path) throws IllegalPath {
    return PathParser.INSTANCE.parse(path);
  }

  static Path encode(String unencoded) {
    return PathParser.INSTANCE.encode(unencoded);
  }

  Path normalise();

  Path resolve(Path other);

  default boolean isEmpty() {
    return this.equals(Path.EMPTY);
  }
}
