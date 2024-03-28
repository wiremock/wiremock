/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.testsupport;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.Diff;

public class WireMatchers {

  public static Matcher<String> equalToJson(final String expectedJson) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Expected:\n" + expectedJson);
      }

      @Override
      public boolean matchesSafely(String actualJson) {
        try {
          JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
          return true;
        } catch (Throwable e) {
          return false;
        }
      }
    };
  }

  public static Matcher<byte[]> bytesEqualToJson(
      final String expectedJson, final JSONCompareMode jsonCompareMode) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Expected:\n" + expectedJson);
      }

      @Override
      public boolean matchesSafely(byte[] actualJson) {
        try {
          JSONAssert.assertEquals(
              expectedJson, Strings.stringFromBytes(actualJson), jsonCompareMode);
          return true;
        } catch (Throwable e) {
          return false;
        }
      }
    };
  }

  public static Matcher<String> equalToJson(
      final String expectedJson, final JSONCompareMode jsonCompareMode) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Expected:\n" + expectedJson);
      }

      @Override
      public boolean matchesSafely(String actualJson) {
        try {
          JSONAssert.assertEquals(expectedJson, actualJson, jsonCompareMode);
          return true;
        } catch (Throwable e) {
          return false;
        }
      }
    };
  }

  public static Matcher<byte[]> equalToBinaryJson(
      final String expectedJson, final JSONCompareMode jsonCompareMode) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Expected:\n" + expectedJson);
      }

      @Override
      public boolean matchesSafely(byte[] actualJson) {
        try {
          JSONAssert.assertEquals(
              expectedJson, Strings.stringFromBytes(actualJson), jsonCompareMode);
          return true;
        } catch (Throwable e) {
          return false;
        }
      }
    };
  }

  public static Matcher<String> equalToXml(final String expected) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(String value) {
        Diff diff =
            DiffBuilder.compare(Input.from(expected))
                .withTest(value)
                .withComparisonController(ComparisonControllers.StopWhenDifferent)
                .ignoreWhitespace()
                .ignoreComments()
                .build();

        return !diff.hasDifferences();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Expected:\n" + expected);
      }
    };
  }

  public static Matcher<String> matches(final String regex) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("Should match " + regex);
      }

      @Override
      public boolean matchesSafely(String actual) {
        return actual.matches(regex);
      }
    };
  }

  public static Matcher<String> matchesMultiLine(final String regex) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("Should match " + regex);
      }

      @Override
      public boolean matchesSafely(String actual) {
        return Pattern.compile(regex, MULTILINE + DOTALL).matcher(actual).matches();
      }
    };
  }

  public static <T> Matcher<Iterable<T>> hasExactly(final Matcher<T>... items) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Collection must match exactly");
      }

      @Override
      public boolean matchesSafely(Iterable<T> actual) {
        Iterator<T> actualIter = actual.iterator();
        for (Matcher<T> matcher : items) {
          if (!matcher.matches(actualIter.next())) {
            return false;
          }
        }

        return !actualIter.hasNext();
      }
    };
  }

  public static <T> Matcher<Iterable<T>> hasExactlyIgnoringOrder(final Matcher<T>... items) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("Collection elements must match, but don't have to be in the same order.");
      }

      @Override
      public boolean matchesSafely(Iterable<T> actual) {
        if (StreamSupport.stream(actual.spliterator(), false).count() != items.length) {
          return false;
        }

        for (final Matcher<T> matcher : items) {
          if (StreamSupport.stream(actual.spliterator(), false)
                  .filter(isMatchFor(matcher))
                  .findAny()
                  .orElse(null)
              == null) {
            return false;
          }
        }

        return true;
      }
    };
  }

  private static <T> Predicate<T> isMatchFor(final Matcher<T> matcher) {
    return matcher::matches;
  }

  public static Matcher<TextFile> fileNamed(final String name) {
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("a text file named " + name);
      }

      @Override
      public boolean matchesSafely(TextFile textFile) {
        return textFile.name().endsWith(name);
      }
    };
  }

  public static Matcher<TextFile> fileWithPath(final String path) {

    String normalizedPath = path.replace('/', File.separatorChar);
    return new TypeSafeMatcher<>() {

      @Override
      public void describeTo(Description desc) {
        desc.appendText("a text file with path " + normalizedPath);
      }

      @Override
      public boolean matchesSafely(TextFile textFile) {
        return textFile.getPath().equals(normalizedPath);
      }
    };
  }

  public static Matcher<Date> isAfter(final String dateString) {
    return new TypeSafeMatcher<>() {
      @Override
      public boolean matchesSafely(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
          Date compareDate = df.parse(dateString);
          return date.after(compareDate);
        } catch (ParseException pe) {
          throw new RuntimeException(pe);
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("A date after " + dateString);
      }
    };
  }

  public static Matcher<Date> isToday() {
    return new TypeSafeMatcher<>() {
      @Override
      public boolean matchesSafely(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String today = df.format(new Date());
        return today.equals(df.format(date));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Today's date");
      }
    };
  }

  public static Matcher<HttpHeader> header(final String key, final String value) {
    return new TypeSafeMatcher<>() {
      @Override
      public boolean matchesSafely(HttpHeader httpHeader) {
        return httpHeader.key().equals(key) && httpHeader.containsValue(value);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("Header %s: %s", key, value));
      }
    };
  }

  public static Matcher<Path> hasFileContaining(final String... contents) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      protected boolean matchesSafely(Path path, Description mismatchDescription) {
        List<File> files = asList(Objects.requireNonNull(path.toFile().listFiles()));
        boolean matched =
            files.stream()
                .anyMatch(
                    file -> {
                      final String fileContents = fileContents(file);

                      return Arrays.stream(contents).allMatch(fileContents::contains);
                    });

        if (files.isEmpty()) {
          mismatchDescription.appendText("there were no files in " + path);
        }

        if (!matched) {
          String allFileContents =
              files.stream().map(WireMatchers::fileContents).collect(Collectors.joining("\n\n"));

          mismatchDescription.appendText(allFileContents);
        }

        return matched;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a file containing all of: " + String.join(", ", contents));
      }
    };
  }

  public static Matcher<String> equalsMultiLine(final String expected) {
    String normalisedExpected = Strings.normaliseLineBreaks(expected);
    return new IsEqual<>(normalisedExpected) {
      @Override
      public boolean matches(Object actualValue) {
        return super.matches(actualValue.toString());
      }
    };
  }

  private static String fileContents(File input) {
    try {
      return FileUtils.readFileToString(input, StandardCharsets.UTF_8);
    } catch (IOException e) {
      return throwUnchecked(e, String.class);
    }
  }

  public static Predicate<StubMapping> withUrl(final String url) {
    return input -> url.equals(input.getRequest().getUrl());
  }

  public static TypeSafeDiagnosingMatcher<StubMapping> stubMappingWithUrl(final String url) {
    return stubMappingWithUrl(urlEqualTo(url));
  }

  public static TypeSafeDiagnosingMatcher<StubMapping> stubMappingWithUrl(
      final UrlPattern urlPattern) {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("a stub mapping with a request URL matching " + urlPattern);
      }

      @Override
      protected boolean matchesSafely(StubMapping item, Description mismatchDescription) {
        return urlPattern.match(item.getRequest().getUrl()).isExactMatch();
      }
    };
  }

  public static ServeEvent findServeEventWithUrl(List<ServeEvent> serveEvents, final String url) {
    return serveEvents.stream()
        .filter(input -> url.equals(input.getRequest().getUrl()))
        .findAny()
        .orElseThrow(NoSuchElementException::new);
  }

  public static StubMapping findMappingWithUrl(List<StubMapping> stubMappings, final String url) {
    return stubMappings.stream()
        .filter(withUrl(url))
        .findAny()
        .orElseThrow(NoSuchElementException::new);
  }

  public static List<StubMapping> findMappingsWithUrl(
      List<StubMapping> stubMappings, final String url) {
    return stubMappings.stream().filter(withUrl(url)).collect(Collectors.toUnmodifiableList());
  }

  public static TypeSafeDiagnosingMatcher<StubMapping> isInAScenario() {
    return new TypeSafeDiagnosingMatcher<>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("a stub mapping with a scenario name");
      }

      @Override
      protected boolean matchesSafely(StubMapping item, Description mismatchDescription) {
        return item.getScenarioName() != null;
      }
    };
  }
}
