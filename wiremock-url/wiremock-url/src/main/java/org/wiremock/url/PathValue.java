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

import static org.wiremock.url.Lazy.lazy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

final class PathValue implements Path {

  private final String path;
  private final MemoisedNormalisable<Path> memoisedNormalisable;

  PathValue(String path) {
    this(path, null);
  }

  PathValue(String path, @Nullable Boolean isNormalForm) {
    this.path = path;
    this.memoisedNormalisable =
        new MemoisedNormalisable<>(this, isNormalForm, this::isNormalFormWork, this::normaliseWork);
  }

  @Override
  public String toString() {
    return path;
  }

  @Override
  public boolean isAbsolute() {
    return !path.isEmpty() && path.charAt(0) == '/';
  }

  /**
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4">RFC 3986 5.2.4.
   *     Remove Dot Segments</a>
   */
  @Override
  public Path normalise() {
    return memoisedNormalisable.normalise();
  }

  private @Nullable Path normaliseWork() {
    if (this.equals(ROOT) || this.equals(EMPTY)) {
      return null;
    }
    var inputBuffer = new StringBuilder(path);
    var outputBuffer = new StringBuilder();
    while (!inputBuffer.isEmpty()) {
      // A. If the input buffer begins with a prefix of "../" or "./", then remove that prefix
      // from the input buffer
      if (remove(inputBuffer, "^..?/")) {
        continue;
      }

      // B. if the input buffer begins with a prefix of "/./" or "/.", where "." is a complete
      // path segment, then replace that prefix with "/" in the input buffer
      if (replace(inputBuffer, "^/.(/|$)", "/")) {
        continue;
      }

      // C. if the input buffer begins with a prefix of "/../" or "/..", where ".." is a complete
      // path segment, then replace that prefix with "/" in the input buffer and remove the last
      // segment and its preceding "/" (if any) from the output buffer
      if (replace(inputBuffer, "^/..(/|$)", "/")) {
        var lastSegment = outputBuffer.lastIndexOf("/");
        if (lastSegment >= 0) {
          outputBuffer.replace(lastSegment, outputBuffer.length(), "");
        }
        continue;
      }

      // D. if the input buffer consists only of "." or "..", then remove that from the input
      // buffer
      if (remove(inputBuffer, "^..?$")) {
        continue;
      }

      // E. move the first path segment in the input buffer to the end of the output buffer,
      // including the initial "/" character (if any) and any subsequent characters up to, but not
      // including, the next "/" character or the end of the input buffer.
      int endOfFirstSegment = getEndOfFirstSegment(inputBuffer);
      outputBuffer.append(inputBuffer, 0, endOfFirstSegment);
      inputBuffer.replace(0, endOfFirstSegment, "");
    }
    var outStr = PathParser.INSTANCE.normalisePercentEncoded(outputBuffer.toString());
    if (outStr.equals(path)) {
      return null;
    } else if (outStr.equals(ROOT.toString())) {
      return ROOT;
    } else {
      return new PathValue(outStr, true);
    }
  }

  @Override
  public boolean isNormalForm() {
    return memoisedNormalisable.isNormalForm();
  }

  private boolean isNormalFormWork() {
    return normalise().equals(this);
  }

  private static int getEndOfFirstSegment(StringBuilder inputBuffer) {
    final int indexOfSlashAtEndOfFirstSegment;
    if (inputBuffer.charAt(0) == '/') {
      indexOfSlashAtEndOfFirstSegment = inputBuffer.indexOf("/", 1);
    } else {
      indexOfSlashAtEndOfFirstSegment = inputBuffer.indexOf("/");
    }
    if (indexOfSlashAtEndOfFirstSegment == -1) {
      return inputBuffer.length();
    }
    return indexOfSlashAtEndOfFirstSegment;
  }

  /*
   * The character `.` does not have its usual regex meaning here. For ease of comparison with
   * the spec it is a placeholder, replaced with the pattern `(?:\.|%2[Ee])` that matches both a
   * literal `.` and the percent encoded form of `.`, either `%2E` or `%2e`.
   */
  private static boolean remove(StringBuilder original, @Language("RegExp") String pattern) {
    return replace(original, pattern, "");
  }

  @Language("RegExp")
  @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
  private static final String DOT = "(?:\\.|%2[Ee])";

  private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();

  /*
   * The character `.` does not have its usual regex meaning here. For ease of comparison with
   * the spec it is a placeholder, replaced with the pattern `(?:\.|%2[Ee])` that matches both a
   * literal `.` and the percent encoded form of `.`, either `%2E` or `%2e`.
   */
  private static boolean replace(
      StringBuilder original, @Language("RegExp") String pattern, String replacement) {
    Pattern p =
        CACHE.computeIfAbsent(
            pattern,
            regex -> Pattern.compile(regex.replaceAll("\\.", Matcher.quoteReplacement(DOT))));
    Matcher matcher = p.matcher(original);
    boolean matches = matcher.find();
    if (matches) {
      original.replace(matcher.start(), matcher.end(), replacement);
    }
    return matches;
  }

  @Override
  public Path resolve(Path other) {
    final Path result;
    if (other.toString().isEmpty()) {
      result = this;
    } else if (other.isAbsolute()) {
      result = other;
    } else {
      var lastIndexOfSlash = this.path.lastIndexOf('/');
      if (lastIndexOfSlash == -1) {
        result = other;
      } else {
        result = PathParser.INSTANCE.parse(this.path.substring(0, lastIndexOfSlash + 1) + other);
      }
    }
    return result.normalise();
  }

  private final Lazy<List<Segment>> segments = lazy(this::buildSegments);

  @Override
  public List<Segment> getSegments() {
    return segments.get();
  }

  private List<Segment> buildSegments() {
    return Arrays.stream(path.split("/", -1)).map(s -> (Segment) new SegmentValue(s)).toList();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Path that) {
      return Objects.equals(this.toString(), that.toString());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(path);
  }
}
