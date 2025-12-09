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
import java.util.LinkedList;
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
      var segments = Arrays.stream(pathStr.split("/", -1)).map(s -> (Segment) new SegmentImpl(s)).toList();
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
      return ROOT.resolve(this);
    }

    @Override
    public org.wiremock.url.Path resolve(org.wiremock.url.Path other) {
      if (other.equals(ROOT)) {
        return this;
      }
      if (other.equals(Path.EMPTY)) {
        return this.normalise();
      }
      var pathStack = new LinkedList<Segment>();
      if (!other.isAbsolute()) {
        pathStack.addAll(this.normalise().segments());
        if (!pathStack.getLast().isEmpty()) {
          pathStack.removeLast();
        }
      } else {
        pathStack.add(Segment.EMPTY);
        pathStack.add(Segment.EMPTY);
      }
      // Handle empty path and single slash early
      List<Segment> otherSegments = other.segments();
      for (Segment candidate : otherSegments) {
        if (candidate.isDotDot()) {
          if (pathStack.size() <= 2) {
            pathStack.clear();
            pathStack.add(Segment.EMPTY);
            pathStack.add(Segment.EMPTY);
          } else {
            if (pathStack.getLast().isEmpty()) {
              pathStack.removeLast();
            }
            pathStack.removeLast();
            pathStack.add(Segment.EMPTY);
          }
        } else if (candidate.isDot() || candidate.isEmpty()) {
          if (!pathStack.getLast().isEmpty()) {
            pathStack.add(Segment.EMPTY);
          }
        } else {
          if (pathStack.getLast().isEmpty()) {
            pathStack.removeLast();
          }
          pathStack.add(candidate);
        }
      }
      if (pathStack.equals(otherSegments)) {
        return other;
      } else {
        String normalizedPath = String.join("/", pathStack);
        return new Path(normalizedPath, pathStack);
      }
    }
  }
}
