/*
 * Copyright (C) 2021-2026 Thomas Akehurst
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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.wiremock.url.Path;

/**
 * The JVM holds a class's initialisation lock while it initialises the classes it needs, and does
 * not report the deadlock when two threads take those locks in opposite orders. They just hang.
 *
 * <p>A cycle is only dangerous if two threads can start it from different classes, so an enum and
 * its constant bodies are fine: they can only be entered through the enum. To remove a dangerous
 * cycle where an interface has constants of its own implementation, declare the constants on a
 * package-private superinterface with no default methods, as {@code PathConstants} does.
 */
class ClassInitialisationCycleTest {

  @Test
  void noClassInitialisationCycleCanBeEnteredFromTwoClasses() {
    JavaClasses classes =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeArchives())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackagesOf(WireMockServer.class, Path.class);

    assertThat(classes.contain(WireMockServer.class)).isTrue();
    assertThat(classes.contain(Path.class)).isTrue();
    assertThat(deadlockableCycles(classes)).isEmpty();
  }

  @Test
  void findsACycleThatCanBeEnteredFromTwoClasses() {
    JavaClasses classes =
        new ClassFileImporter()
            .importClasses(Deadlockable.class, DeadlockableValue.class, EntersBothClasses.class);

    assertThat(deadlockableCycles(classes))
        .containsExactly(Set.of(Deadlockable.class.getName(), DeadlockableValue.class.getName()));
  }

  @Test
  void ignoresACycleThatCanOnlyBeEnteredFromOneClass() {
    JavaClasses classes =
        new ClassFileImporter()
            .importClasses(SingleEntry.class, SingleEntry.Value.class, EntersOneClass.class);

    assertThat(deadlockableCycles(classes)).isEmpty();
  }

  private static List<Set<String>> deadlockableCycles(JavaClasses classes) {
    Map<JavaClass, Set<JavaClass>> triggers = new HashMap<>();
    classes.forEach(javaClass -> triggers.put(javaClass, initialisationTriggeredBy(javaClass)));
    Map<JavaClass, Set<JavaClass>> reachable = new HashMap<>();
    triggers.keySet().forEach(c -> reachable.put(c, reachableFrom(c, triggers)));

    return triggers.keySet().stream()
        .map(c -> cycleContaining(c, reachable))
        .filter(cycle -> cycle.size() > 1)
        .distinct()
        .filter(cycle -> entryPoints(cycle, classes, triggers).size() > 1)
        .map(
            cycle ->
                (Set<String>)
                    cycle.stream().map(JavaClass::getName).collect(toCollection(TreeSet::new)))
        .toList();
  }

  private static Set<JavaClass> cycleContaining(
      JavaClass javaClass, Map<JavaClass, Set<JavaClass>> reachable) {
    return reachable.get(javaClass).stream()
        .filter(other -> reachable.getOrDefault(other, Set.of()).contains(javaClass))
        .collect(toSet());
  }

  private static Set<JavaClass> entryPoints(
      Set<JavaClass> cycle, JavaClasses classes, Map<JavaClass, Set<JavaClass>> triggers) {
    Set<JavaClass> entries = new HashSet<>();
    classes.stream()
        .filter(javaClass -> !cycle.contains(javaClass))
        .forEach(
            outside -> {
              triggers.get(outside).stream().filter(cycle::contains).forEach(entries::add);
              outside.getCodeUnits().stream()
                  .flatMap(ClassInitialisationCycleTest::classesInitialisedBy)
                  .filter(cycle::contains)
                  .forEach(entries::add);
            });
    return entries;
  }

  private static Set<JavaClass> reachableFrom(
      JavaClass start, Map<JavaClass, Set<JavaClass>> triggers) {
    Set<JavaClass> seen = new HashSet<>(Set.of(start));
    ArrayDeque<JavaClass> toVisit = new ArrayDeque<>(seen);
    while (!toVisit.isEmpty()) {
      triggers.getOrDefault(toVisit.pop(), Set.of()).stream()
          .filter(seen::add)
          .forEach(toVisit::add);
    }
    return seen;
  }

  private static Set<JavaClass> initialisationTriggeredBy(JavaClass javaClass) {
    Set<JavaClass> triggered = new HashSet<>();
    javaClass.getRawSuperclass().ifPresent(triggered::add);
    if (!javaClass.isInterface()) {
      javaClass.getAllRawInterfaces().stream()
          .filter(ClassInitialisationCycleTest::declaresDefaultMethod)
          .forEach(triggered::add);
    }
    javaClass
        .getStaticInitializer()
        .ifPresent(
            initialiser ->
                codeReachableFrom(initialiser).stream()
                    .flatMap(ClassInitialisationCycleTest::classesInitialisedBy)
                    .forEach(triggered::add));
    triggered.remove(javaClass);
    return triggered;
  }

  private static boolean declaresDefaultMethod(JavaClass javaInterface) {
    return javaInterface.getMethods().stream()
        .anyMatch(
            method ->
                !method.getModifiers().contains(JavaModifier.ABSTRACT)
                    && !method.getModifiers().contains(JavaModifier.STATIC));
  }

  private static Set<JavaCodeUnit> codeReachableFrom(JavaCodeUnit start) {
    Set<JavaCodeUnit> seen = new HashSet<>(Set.of(start));
    ArrayDeque<JavaCodeUnit> toVisit = new ArrayDeque<>(seen);
    while (!toVisit.isEmpty()) {
      JavaCodeUnit code = toVisit.pop();
      Stream.concat(
              code.getMethodCallsFromSelf().stream()
                  .map(call -> call.getTarget().resolveMember())
                  .flatMap(Optional::stream),
              code.getConstructorCallsFromSelf().stream()
                  .map(call -> call.getTarget().resolveMember())
                  .flatMap(Optional::stream))
          .map(JavaCodeUnit.class::cast)
          .filter(seen::add)
          .forEach(toVisit::add);
    }
    return seen;
  }

  private static Stream<JavaClass> classesInitialisedBy(JavaCodeUnit code) {
    Stream<JavaClass> constructed =
        code.getConstructorCallsFromSelf().stream()
            .filter(call -> !isSuperOrThisCall(call.getTargetOwner(), code))
            .map(call -> call.getTargetOwner());
    Stream<JavaMember> staticMembers =
        Stream.concat(
                code.getMethodCallsFromSelf().stream()
                    .map(call -> call.getTarget().resolveMember())
                    .flatMap(Optional::stream)
                    .map(JavaMember.class::cast),
                code.getFieldAccesses().stream()
                    .map(access -> access.getTarget().resolveMember())
                    .flatMap(Optional::stream)
                    .map(JavaMember.class::cast))
            .filter(member -> member.getModifiers().contains(JavaModifier.STATIC));
    return Stream.concat(constructed, staticMembers.map(JavaMember::getOwner));
  }

  private static boolean isSuperOrThisCall(JavaClass target, JavaCodeUnit code) {
    return code.isConstructor()
        && (code.getOwner().equals(target) || code.getOwner().isAssignableTo(target.getName()));
  }

  interface Deadlockable {
    Deadlockable EMPTY = new DeadlockableValue();

    default void aDefaultMethod() {}
  }

  static final class DeadlockableValue implements Deadlockable {}

  static final class EntersBothClasses {
    Collection<Deadlockable> both() {
      return List.of(Deadlockable.EMPTY, new DeadlockableValue());
    }
  }

  abstract static class SingleEntry {
    static final SingleEntry ONLY = new Value();

    private static final class Value extends SingleEntry {}
  }

  static final class EntersOneClass {
    SingleEntry only() {
      return SingleEntry.ONLY;
    }
  }
}
