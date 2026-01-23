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
package org.wiremock.url.jackson2;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.wiremock.stringparser.ParsedString;
import org.wiremock.stringparser.StringParser;
import org.wiremock.stringparser.jackson2.ParsedStringModule;
import org.wiremock.url.PercentEncoded;

class WireMockUrlModuleRegistrationTest {

  @Test
  void allPublicParsedStringSubtypesAreRegistered() {
    Set<Class<?>> parsedStringSubtypes = findPublicParsedStringSubtypes();
    Set<Class<?>> registeredTypes = getRegisteredTypes(new WireMockUrlModule());

    assertThat(registeredTypes)
        .as("All public ParsedString subtypes in org.wiremock.url should be registered")
        .containsExactlyInAnyOrderElementsOf(parsedStringSubtypes);
  }

  private Set<Class<?>> findPublicParsedStringSubtypes() {
    try (ScanResult scanResult =
        new ClassGraph().enableClassInfo().acceptPackages("org.wiremock.url").scan()) {

      return scanResult.getClassesImplementing(ParsedString.class).stream()
          .filter(classInfo -> Modifier.isPublic(classInfo.getModifiers()))
          .filter(ClassInfo::isInterface)
          .filter(classInfo -> !classInfo.getName().equals(ParsedString.class.getName()))
          .filter(classInfo -> !classInfo.getName().equals(PercentEncoded.class.getName()))
          .map(ClassInfo::loadClass)
          .collect(Collectors.toSet());
    }
  }

  private Set<Class<?>> getRegisteredTypes(ParsedStringModule module) {
    List<StringParser<?>> deserializers = module.getStringParsers();

    return deserializers.stream().map(StringParser::getType).collect(Collectors.toSet());
  }
}
