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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.diff.ComparisonType.NAMESPACE_URI;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EqualToXmlPatternNamespaceTest {

  @ParameterizedTest
  @MethodSource("namespaceChanges")
  void namespacePresenceIsComparedInBothDirections(String withoutNamespace, String withNamespace) {
    for (EqualToXmlPattern.NamespaceAwareness awareness :
        new EqualToXmlPattern.NamespaceAwareness[] {
          null, EqualToXmlPattern.NamespaceAwareness.STRICT
        }) {
      MatchResult added =
          equalToXml(withoutNamespace).withNamespaceAwareness(awareness).match(withNamespace);
      MatchResult removed =
          equalToXml(withNamespace).withNamespaceAwareness(awareness).match(withoutNamespace);

      assertFalse(added.isExactMatch());
      assertTrue(added.getDistance() > 0.0);
      assertFalse(removed.isExactMatch());
      assertTrue(removed.getDistance() > 0.0);
    }
  }

  @ParameterizedTest
  @MethodSource("namespaceChanges")
  void namespacePresenceCanBeExcluded(String withoutNamespace, String withNamespace) {
    MatchResult added =
        equalToXml(withoutNamespace).exemptingComparisons(NAMESPACE_URI).match(withNamespace);
    MatchResult removed =
        equalToXml(withNamespace).exemptingComparisons(NAMESPACE_URI).match(withoutNamespace);

    assertTrue(added.isExactMatch());
    assertEquals(0.0, added.getDistance());
    assertTrue(removed.isExactMatch());
    assertEquals(0.0, removed.getDistance());
  }

  @ParameterizedTest
  @MethodSource("namespaceChanges")
  void legacyNamespaceAwarenessStillIgnoresAddedNamespaces(
      String withoutNamespace, String withNamespace) {
    MatchResult added =
        equalToXml(withoutNamespace)
            .withNamespaceAwareness(EqualToXmlPattern.NamespaceAwareness.LEGACY)
            .match(withNamespace);

    assertTrue(added.isExactMatch());
    assertEquals(0.0, added.getDistance());
  }

  private static Stream<Arguments> namespaceChanges() {
    return Stream.of(
        Arguments.of(
            "<a:Root xmlns:a=\"http://whatever/\"><Child/></a:Root>",
            "<a:Root xmlns:a=\"http://whatever/\"><a:Child/></a:Root>"),
        Arguments.of(
            "<a:Root xmlns:a=\"http://whatever/\"><Child/></a:Root>",
            "<a:Root xmlns:a=\"http://whatever/\"><Child xmlns=\"http://somethingnew/\"/></a:Root>"));
  }
}
