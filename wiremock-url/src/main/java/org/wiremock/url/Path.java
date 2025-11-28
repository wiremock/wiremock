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

import static org.wiremock.url.Constants.alwaysIllegal;

import java.util.regex.Pattern;

public interface Path extends PctEncoded {

  Path EMPTY = new PathParser.Path("");

  boolean isAbsolute();

  static Path parse(CharSequence path) throws IllegalPath {
    return PathParser.INSTANCE.parse(path);
  }
}

class PathParser implements CharSequenceParser<Path> {

  static final PathParser INSTANCE = new PathParser();

  final String pathRegex = "[^#?" + alwaysIllegal + "]*";
  private final Pattern pathPattern = Pattern.compile("^" + pathRegex + "$");

  @Override
  public Path parse(CharSequence stringForm) {
    String pathStr = stringForm.toString();
    if (pathPattern.matcher(pathStr).matches()) {
      return new Path(pathStr);
    } else {
      throw new IllegalPath(pathStr);
    }
  }

  record Path(String path) implements org.wiremock.url.Path {

    @Override
    public int length() {
      return path.length();
    }

    @Override
    public char charAt(int index) {
      return path.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return path.subSequence(start, end);
    }

    @Override
    public String toString() {
      return path;
    }

    @Override
    public boolean isAbsolute() {
      return path.charAt(0) == '/';
    }

    @Override
    public String decode() {
      throw new UnsupportedOperationException();
    }
  }
}
