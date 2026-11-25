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
package com.github.tomakehurst.wiremock.archunit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The native binary's reflection metadata is traced from the test suite, so a model class that no
 * test happens to serialise would be missing and fail only in the native binary. This guards the
 * classes Jackson is told about explicitly; un-annotated beans are only covered by the tracing.
 */
class NativeImageJacksonMetadataTest {

  private static final Path NATIVE_IMAGE_DIR = Path.of("wiremock-standalone/src/native-image");

  @Test
  void everyJacksonAnnotatedClassIsRegisteredForReflection() throws IOException {
    Map<String, JsonNode> registered = registeredTypes();

    List<String> missing =
        new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeArchives())
                .withImportOption(new ImportOption.DoNotIncludeJars())
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.github.tomakehurst.wiremock", "org.wiremock")
                .stream()
                .filter(NativeImageJacksonMetadataTest::usesJackson)
                .flatMap(type -> problems(type, registered.get(type.getName())))
                .sorted()
                .toList();

    assertThat(missing)
        .as(
            "Missing from %s. Trace a test that serialises them (see updateNativeMetadata) or add"
                + " them to the hand-written metadata under resources/",
            NATIVE_IMAGE_DIR)
        .isEmpty();
  }

  private static Stream<String> problems(JavaClass type, JsonNode entry) {
    if (entry == null) {
      return Stream.of(type.getName());
    }
    return type.getConstructors().stream()
        .filter(constructor -> constructor.isAnnotatedWith(JsonCreator.class))
        .filter(constructor -> !constructorRegistered(entry, constructor))
        .map(JavaConstructor::getFullName);
  }

  private static boolean constructorRegistered(JsonNode entry, JavaConstructor constructor) {
    if (entry.path("allDeclaredConstructors").asBoolean()
        || (constructor.getModifiers().contains(JavaModifier.PUBLIC)
            && entry.path("allPublicConstructors").asBoolean())) {
      return true;
    }
    List<String> parameterTypes =
        constructor.getRawParameterTypes().stream().map(JavaClass::getName).toList();
    for (JsonNode method : entry.path("methods")) {
      if (method.path("name").asText().equals("<init>")) {
        List<String> registeredTypes = new ArrayList<>();
        method.path("parameterTypes").forEach(p -> registeredTypes.add(p.asText()));
        if (registeredTypes.equals(parameterTypes)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean usesJackson(JavaClass type) {
    return Stream.of(
            type.getAnnotations().stream(),
            type.getFields().stream().flatMap(f -> f.getAnnotations().stream()),
            type.getMethods().stream().flatMap(m -> m.getAnnotations().stream()),
            type.getConstructors().stream().flatMap(c -> c.getAnnotations().stream()),
            type.getCodeUnits().stream()
                .flatMap(c -> c.getParameters().stream())
                .flatMap(p -> p.getAnnotations().stream()))
        .flatMap(s -> s)
        .map(JavaAnnotation::getRawType)
        .anyMatch(annotation -> annotation.getPackageName().startsWith("com.fasterxml.jackson"));
  }

  private static Map<String, JsonNode> registeredTypes() throws IOException {
    Map<String, JsonNode> types = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try (Stream<Path> files = Files.walk(NATIVE_IMAGE_DIR)) {
      for (Path file :
          files
              .filter(f -> f.getFileName().toString().equals("reachability-metadata.json"))
              .toList()) {
        for (JsonNode entry : mapper.readTree(file.toFile()).path("reflection")) {
          if (entry.path("type").isTextual()) {
            types.merge(entry.path("type").asText(), entry, NativeImageJacksonMetadataTest::merge);
          }
        }
      }
    }
    return types;
  }

  private static JsonNode merge(JsonNode first, JsonNode second) {
    ObjectNode merged = first.deepCopy();
    second.fields().forEachRemaining(field -> merged.set(field.getKey(), field.getValue()));
    if (first.has("methods") && second.has("methods")) {
      merged
          .putArray("methods")
          .addAll((ArrayNode) first.get("methods"))
          .addAll((ArrayNode) second.get("methods"));
    }
    return merged;
  }
}
