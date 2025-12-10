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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public interface Path {

  Path EMPTY = PathParser.INSTANCE.parse("");
  Path ROOT = PathParser.INSTANCE.parse("/");

  boolean isAbsolute();

  List<Segment> segments();

  static Path parse(CharSequence path) throws IllegalPath {
    return PathParser.INSTANCE.parse(path);
  }

  Path normalise();

  Path resolve(Path other);
}

class PathParser implements CharSequenceParser<Path> {

  static final PathParser INSTANCE = new PathParser();

  final String pathRegex = "[^#?" + alwaysIllegal + "]*";
  private final Pattern pathPattern = Pattern.compile("^" + pathRegex + "$");

  @Override
  public Path parse(CharSequence stringForm) {
    String pathStr = stringForm.toString();
    if (pathPattern.matcher(pathStr).matches()) {
      var segments =
          Arrays.stream(pathStr.split("/", -1)).map(s -> (Segment) new SegmentImpl(s)).toList();
      return new Path(pathStr, segments);
    } else {
      throw new IllegalPath(pathStr);
    }
  }

  record Path(String path, List<Segment> segments) implements org.wiremock.url.Path {

    @Override
    public String toString() {
      return path;
    }

    @Override
    public boolean isAbsolute() {
      return !path.isEmpty() && path.charAt(0) == '/';
    }

    @Override
    public org.wiremock.url.Path normalise() {
      if (this.equals(ROOT) || this.equals(EMPTY)) {
        return this;
      }
      var original = path;
      var output = new StringBuilder();
      while (!original.isEmpty()) {
        if (original.startsWith("../")) {
          original = original.substring(3);
        } else if (original.startsWith("./")) {
          original = original.substring(2);
        } else if (original.startsWith("/./")) {
          original = original.substring(2);
        } else if (original.equals("/.")) {
          original = "/" + original.substring(2);
        } else if (original.startsWith("/../")) {
          original = original.substring(3);
          var lastSegment = output.lastIndexOf("/");
          if (lastSegment >= 0) {
            output.replace(lastSegment, output.length(), "");
          }
        } else if (original.equals("/..")) {
          original = "/";
          var lastSegment = output.lastIndexOf("/");
          if (lastSegment >= 0) {
            output.replace(lastSegment, output.length(), "");
          }
        } else if (original.equals(".") || original.equals("..")) {
          original = "";
        } else {
          int end;
          if (original.startsWith("/")) {
            end = original.indexOf('/', 1);
          } else {
            end = original.indexOf('/');
          }
          if (end == -1) {
            end = original.length();
          }
          output.append(original, 0, end);
          original = original.substring(end);
        }
      }
      var outStr = output.toString();
      if (outStr.equals(path)) {
        return this;
      } else if (outStr.equals(ROOT.toString())) {
        return ROOT;
      } else {
        return PathParser.INSTANCE.parse(outStr);
      }
    }

    @Override
    public org.wiremock.url.Path resolve(org.wiremock.url.Path other) {
      final org.wiremock.url.Path result;
      if (other.toString().isEmpty()) {
        result = this;
      } else if (other.isAbsolute()) {
        result = other;
      } else if (this.path.endsWith("/")) {
        result = PathParser.INSTANCE.parse(this.path + other);
      } else {
        result = PathParser.INSTANCE.parse(this.path + "/../" + other);
      }
      return result.normalise();
    }
  }
}
