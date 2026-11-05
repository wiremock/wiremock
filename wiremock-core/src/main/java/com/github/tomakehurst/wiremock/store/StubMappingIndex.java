/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import static com.github.tomakehurst.wiremock.common.SortedConcurrentPrioritisableSet.PRIORITY_THEN_REVERSE_INSERTION_ORDER;
import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsOneOf;
import com.github.tomakehurst.wiremock.matching.PathTemplatePattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class StubMappingIndex {

  private static final Pattern LITERAL_START_OF_REGEX =
      Pattern.compile("\\^?(?<literal>[\\w/~%,;:=@!&'-]*)(?<quantifier>[?*+{]?)");

  private final Map<String, Bucket> byExactPath = new ConcurrentHashMap<>();
  private final Map<String, Bucket> byPathPrefix = new ConcurrentHashMap<>();
  private final AtomicInteger longestPathPrefix = new AtomicInteger();
  private final Bucket withoutConcretePath = new Bucket();
  private final Map<RequestMethod, Bucket> byMethod = new ConcurrentHashMap<>();
  private final Bucket forAnyMethod = new Bucket();

  void add(StubMapping stub) {
    slotsFor(stub).forEach(slot -> slot.add(stub));
  }

  void remove(StubMapping stub) {
    slotsFor(stub).forEach(slot -> slot.remove(stub));
    longestPathPrefix.set(byPathPrefix.keySet().stream().mapToInt(String::length).max().orElse(0));
  }

  void clear() {
    byExactPath.clear();
    byPathPrefix.clear();
    withoutConcretePath.clear();
    byMethod.clear();
    forAnyMethod.clear();
    longestPathPrefix.set(0);
  }

  @Override
  public String toString() {
    return "StubMappingIndex{byExactPath="
        + byExactPath.keySet()
        + ", byPathPrefix="
        + byPathPrefix.keySet()
        + ", longestPathPrefix="
        + longestPathPrefix.get()
        + ", withoutConcretePath="
        + withoutConcretePath.size()
        + ", byMethod="
        + byMethod.keySet()
        + ", forAnyMethod="
        + forAnyMethod.size()
        + "}";
  }

  Stream<StubMapping> candidatesFor(Request request) {
    Candidates byPath = candidatesForPath(pathOf(request));
    Candidates byMethod = candidatesForMethod(request.getMethod());
    return (byPath.size <= byMethod.size ? byPath : byMethod).inOrder();
  }

  private Stream<Slot> slotsFor(StubMapping stub) {
    return Stream.concat(Stream.of(pathSlotFor(stub)), methodSlotsFor(stub));
  }

  private Slot pathSlotFor(StubMapping stub) {
    UrlPattern url = stub.getRequest().getUrlMatcher();
    return exactPathOf(url)
        .<Slot>map(path -> new MapSlot<>(byExactPath, path))
        .or(() -> pathPrefixOf(url).map(this::pathPrefixSlot))
        .orElse(withoutConcretePath);
  }

  private Stream<Slot> methodSlotsFor(StubMapping stub) {
    RequestMethod method = stub.getRequest().getMethod();
    if (method instanceof IsOneOf oneOf) {
      return oneOf.getMethods().stream().map(this::methodSlot);
    } else if (method.getClass() == RequestMethod.class && !method.equals(RequestMethod.ANY)) {
      return Stream.of(methodSlot(method));
    } else {
      return Stream.of(forAnyMethod);
    }
  }

  private Slot pathPrefixSlot(String prefix) {
    longestPathPrefix.accumulateAndGet(prefix.length(), Math::max);
    return new MapSlot<>(byPathPrefix, prefix);
  }

  private Slot methodSlot(RequestMethod method) {
    return new MapSlot<>(byMethod, method);
  }

  private static Optional<String> exactPathOf(UrlPattern url) {
    return url.getPattern() instanceof EqualToPattern equalTo
            && equalTo.getClass() == EqualToPattern.class
            && !TRUE.equals(equalTo.getCaseInsensitive())
        ? Optional.of(Urls.getPathPart(url)).filter(path -> path.startsWith("/"))
        : Optional.empty();
  }

  private static Optional<String> pathPrefixOf(UrlPattern url) {
    StringValuePattern pattern = url.getPattern();
    if (pattern instanceof PathTemplatePattern template) {
      return completeSegmentsOf(template.getPathTemplate().literalPrefix());
    } else if (pattern instanceof RegexPattern regex && regex.getClass() == RegexPattern.class) {
      return completeSegmentsOf(literalPrefixOf(regex.getMatches()));
    } else {
      return Optional.empty();
    }
  }

  private static String literalPrefixOf(String regex) {
    Matcher literalStart = LITERAL_START_OF_REGEX.matcher(regex);
    if (regex.contains("|") || !literalStart.lookingAt()) {
      return "";
    }
    String literal = literalStart.group("literal");
    boolean lastCharacterIsQuantified = !literalStart.group("quantifier").isEmpty();
    return lastCharacterIsQuantified && !literal.isEmpty()
        ? literal.substring(0, literal.length() - 1)
        : literal;
  }

  private static Optional<String> completeSegmentsOf(String literalPrefix) {
    int endOfLastCompleteSegment = literalPrefix.lastIndexOf('/');
    return literalPrefix.startsWith("/") && endOfLastCompleteSegment > 0
        ? Optional.of(literalPrefix.substring(0, endOfLastCompleteSegment))
        : Optional.empty();
  }

  // Per-request hot path: a stream pipeline costs more than the lookups it would wrap here
  private Candidates candidatesForPath(String path) {
    Candidates candidates = new Candidates();
    candidates.addIfNotEmpty(byExactPath.get(path));
    candidates.addIfNotEmpty(withoutConcretePath);
    int longestPrefix = longestPathPrefix.get();
    for (int slash = path.indexOf('/', 1);
        slash > 0 && slash <= longestPrefix;
        slash = path.indexOf('/', slash + 1)) {
      candidates.addIfNotEmpty(byPathPrefix.get(path.substring(0, slash)));
    }
    return candidates;
  }

  private Candidates candidatesForMethod(RequestMethod method) {
    Candidates candidates = new Candidates();
    candidates.addIfNotEmpty(byMethod.get(method));
    candidates.addIfNotEmpty(forAnyMethod);
    return candidates;
  }

  private static String pathOf(Request request) {
    return request.getPathAndQueryWithoutPrefix().getPath().toString();
  }

  private static final class Candidates {
    private final List<Bucket> buckets = new ArrayList<>(4);
    private int size;

    void addIfNotEmpty(Bucket bucket) {
      int bucketSize = bucket == null ? 0 : bucket.size();
      if (bucketSize > 0) {
        buckets.add(bucket);
        size += bucketSize;
      }
    }

    Stream<StubMapping> inOrder() {
      return switch (buckets.size()) {
        case 0 -> Stream.empty();
        case 1 -> buckets.get(0).stream();
        default -> StreamSupport.stream(
            spliteratorUnknownSize(new OrderedMerge(buckets), ORDERED | NONNULL), false);
      };
    }
  }

  private interface Slot {
    void add(StubMapping stub);

    void remove(StubMapping stub);
  }

  private record MapSlot<K>(Map<K, Bucket> buckets, K key) implements Slot {
    @Override
    public void add(StubMapping stub) {
      buckets.compute(
          key,
          (k, bucket) -> {
            Bucket target = bucket == null ? new Bucket() : bucket;
            target.add(stub);
            return target;
          });
    }

    @Override
    public void remove(StubMapping stub) {
      buckets.computeIfPresent(
          key,
          (k, bucket) -> {
            bucket.remove(stub);
            return bucket.size() == 0 ? null : bucket;
          });
    }
  }

  private static final class Bucket implements Slot {
    private final ConcurrentSkipListSet<StubMapping> stubs =
        new ConcurrentSkipListSet<>(PRIORITY_THEN_REVERSE_INSERTION_ORDER);
    private final AtomicInteger size = new AtomicInteger();

    @Override
    public void add(StubMapping stub) {
      if (stubs.add(stub)) {
        size.incrementAndGet();
      }
    }

    @Override
    public void remove(StubMapping stub) {
      if (stubs.remove(stub)) {
        size.decrementAndGet();
      }
    }

    void clear() {
      stubs.forEach(this::remove);
    }

    int size() {
      return size.get();
    }

    Iterator<StubMapping> iterator() {
      return stubs.iterator();
    }

    Stream<StubMapping> stream() {
      return stubs.stream();
    }
  }

  private static final class OrderedMerge implements Iterator<StubMapping> {
    private final PriorityQueue<Head> heads =
        new PriorityQueue<>(comparing(Head::stub, PRIORITY_THEN_REVERSE_INSERTION_ORDER));

    OrderedMerge(List<Bucket> sortedSources) {
      sortedSources.forEach(bucket -> enqueueHeadOf(bucket.iterator()));
    }

    @Override
    public boolean hasNext() {
      return !heads.isEmpty();
    }

    @Override
    public StubMapping next() {
      Head head = heads.remove();
      enqueueHeadOf(head.rest());
      return head.stub();
    }

    private void enqueueHeadOf(Iterator<StubMapping> source) {
      if (source.hasNext()) {
        heads.add(new Head(source.next(), source));
      }
    }

    private record Head(StubMapping stub, Iterator<StubMapping> rest) {}
  }
}
