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

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.common.SortedConcurrentPrioritisableSet.PRIORITY_THEN_REVERSE_INSERTION_ORDER;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.HEAD;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.isNoneOf;
import static com.github.tomakehurst.wiremock.http.RequestMethod.isOneOf;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StubMappingIndexTest {

  private static final String EMPTY_INDEX =
      "StubMappingIndex{byExactPath=[], byPathPrefix=[], withoutConcretePath=0, byMethod=[], forAnyMethod=0}";

  private final StubMappingIndex index = new StubMappingIndex();
  private final List<StubMapping> allStubs = new ArrayList<>();
  private long insertionIndex = 0;

  @Test
  void selectsTheRequestPathBucketWhenItIsSmallerThanTheMethodBucket() {
    StubMapping onPath = add(get(urlPathEqualTo("/a/x")));
    add(get(urlEqualTo("/b")));
    add(get(urlEqualTo("/b")));

    assertThat(candidatesFor(GET, "/a/x"), contains(onPath));
  }

  @Test
  void selectsTheRequestMethodBucketWhenItIsSmallerThanThePathBucket() {
    StubMapping withMethod = add(post(urlEqualTo("/a")));
    add(get(urlEqualTo("/a")));
    add(get(urlEqualTo("/a")));

    assertThat(candidatesFor(POST, "/a"), contains(withMethod));
  }

  @Test
  void pathCandidatesIncludeEveryConcretePrefixOfTheRequestPathAndStubsWithoutOne() {
    StubMapping exact = add(get(urlEqualTo("/a/b/c?q=1")));
    StubMapping template = add(get(urlPathTemplate("/a/b/{id}")));
    StubMapping partialSegmentTemplate = add(get(urlPathTemplate("/a/b{suffix}")));
    StubMapping regex = add(get(urlMatching("/a/b/.*")));
    StubMapping anything = add(get(anyUrl()));
    add(get(urlEqualTo("/a/b/d")));
    add(get(urlPathTemplate("/z/{id}")));
    add(post(urlEqualTo("/elsewhere")));
    add(post(urlEqualTo("/elsewhere")));
    add(post(urlEqualTo("/elsewhere")));
    add(post(urlEqualTo("/elsewhere")));
    add(post(urlEqualTo("/elsewhere")));
    add(post(urlEqualTo("/elsewhere")));

    assertThat(
        candidatesFor(POST, "/a/b/c?q=1"),
        contains(anything, regex, partialSegmentTemplate, template, exact));
  }

  @Test
  void methodCandidatesIncludeStubsForAnyMethodOrOneOfSeveralOrNoneOfSeveral() {
    StubMapping anyMethod = add(any(urlEqualTo("/a")));
    StubMapping oneOf = add(request(isOneOf(GET, HEAD), urlEqualTo("/a")));
    StubMapping noneOf = add(request(isNoneOf(POST), urlEqualTo("/a")));
    add(post(urlEqualTo("/a")));
    add(get(urlEqualTo("/a")));
    add(get(urlEqualTo("/a")));
    add(get(urlEqualTo("/a")));

    assertThat(candidatesFor(HEAD, "/a"), contains(noneOf, oneOf, anyMethod));
  }

  @Test
  void candidatesAreInTheSameOrderAsAllStubs() {
    StubMapping lowPriority = add(get(urlEqualTo("/a")).atPriority(9));
    StubMapping firstAdded = add(get(anyUrl()));
    StubMapping highPriority = add(get(urlPathTemplate("/a/{id}")).atPriority(1));
    StubMapping lastAdded = add(get(urlEqualTo("/a/1")));

    assertThat(candidatesFor(GET, "/a/1"), contains(highPriority, lastAdded, firstAdded));
    assertThat(candidatesFor(GET, "/a"), contains(firstAdded, lowPriority));
  }

  @Test
  void removedStubsAreNotCandidates() {
    StubMapping kept = add(get(urlEqualTo("/a")));
    StubMapping removed = add(get(urlEqualTo("/a")));

    index.remove(removed);

    assertThat(candidatesFor(GET, "/a"), contains(kept));
  }

  @Test
  void clearedStubsAreNotCandidates() {
    add(get(urlEqualTo("/a")));

    index.clear();

    assertThat(candidatesFor(GET, "/a"), empty());
  }

  @Test
  void stubsWhoseLiteralStartIsOneSegmentAreCandidatesForOneSegmentPaths() {
    StubMapping template = add(get(urlPathTemplate("/{id}")));
    StubMapping regex = add(get(urlPathMatching("/a.*")));

    assertThat(candidatesFor(GET, "/abc"), contains(regex, template));
  }

  @Test
  void caseInsensitiveAndRelativeUrlStubsAreCandidatesForEveryPath() {
    StubMapping caseInsensitive = add(get(new UrlPattern(new EqualToPattern("/A", true), false)));
    StubMapping relative = add(get(urlEqualTo("a")));

    assertThat(candidatesFor(GET, "/a"), contains(relative, caseInsensitive));
  }

  @Test
  void removingEveryStubLeavesNothingBehind() {
    List<StubMapping> stubs = addOneStubPerKindOfBucket();

    stubs.forEach(index::remove);

    assertThat(index, hasToString(EMPTY_INDEX));
  }

  @Test
  void clearingLeavesNothingBehind() {
    addOneStubPerKindOfBucket();

    index.clear();

    assertThat(index, hasToString(EMPTY_INDEX));
  }

  @Test
  void candidatesForAnyRequestIncludeEveryStubThatMatchesItInOrder() {
    add(get(urlEqualTo("/a/b?x=1")));
    add(get(urlPathEqualTo("/a/b")));
    add(post(urlPathTemplate("/a/{id}")));
    add(get(urlPathTemplate("/a/b/**")));
    add(put(urlMatching("/a/.*")));
    add(any(urlPathMatching("^/a/b/[0-9]+$")));
    add(get(urlMatching("/z|/a/b")));
    add(delete(anyUrl()));
    add(request(isOneOf(GET, HEAD), urlEqualTo("/a")));
    add(request(isNoneOf(POST), urlPathEqualTo("/a/b")));
    add(get(urlPathTemplate("/{top}")));
    add(get(urlPathMatching("(?i)/A/B")));
    add(get(new UrlPattern(new EqualToPattern("/A/B", true), false)));
    add(get(urlEqualTo("/a/b")).atPriority(1));
    add(post(urlEqualTo("/a/b")).atPriority(9));

    Stream.of("/a", "/a/b", "/a/b?x=1", "/a/b/", "/a/b/12", "/A/B", "/abc", "/", "/z")
        .flatMap(
            url -> Stream.of(GET, POST, PUT, DELETE, HEAD).map(method -> requestFor(method, url)))
        .forEach(
            request ->
                assertThat(
                    request.getMethod() + " " + request.getUrl(),
                    matching(index.candidatesFor(request), request),
                    is(matching(allStubs.stream(), request))));
  }

  private List<StubMapping> addOneStubPerKindOfBucket() {
    return List.of(
        add(get(urlEqualTo("/a"))),
        add(get(urlPathTemplate("/a/{id}"))),
        add(request(isOneOf(GET, POST), anyUrl())),
        add(any(urlMatching(".*"))));
  }

  private static List<StubMapping> matching(Stream<StubMapping> stubs, Request request) {
    return stubs.filter(stub -> stub.getRequest().isMatchedBy(request, Map.of())).toList();
  }

  private StubMapping add(MappingBuilder mappingBuilder) {
    StubMapping stub = mappingBuilder.build().withInsertionIndex(insertionIndex++);
    index.add(stub);
    allStubs.add(stub);
    allStubs.sort(PRIORITY_THEN_REVERSE_INSERTION_ORDER);
    return stub;
  }

  private static Request requestFor(RequestMethod method, String url) {
    return mockRequest().method(method).url(url);
  }

  private List<StubMapping> candidatesFor(RequestMethod method, String url) {
    return index.candidatesFor(requestFor(method, url)).toList();
  }
}
