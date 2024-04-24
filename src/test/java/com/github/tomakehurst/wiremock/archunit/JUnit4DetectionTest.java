/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.WireMockJUnitRuleTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.junit.WireMockRuleFailOnUnmatchedRequestsTest;
import com.github.tomakehurst.wiremock.junit.WireMockStaticRule;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import java.util.stream.Stream;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Limit further usage of JUnit 4 throughout our code base, as we want to migrate to JUnit Jupiter
 * as per https://github.com/wiremock/wiremock/pull/1635. When violations are found either migrate
 * the affected tests to JUnit Jupiter if reasonably possible, or if not feasible right now instead
 * update the freeze store of accepted violations.
 */
@AnalyzeClasses(
    packagesOf = WireMockServer.class,
    importOptions = {ImportOption.DoNotIncludeArchives.class, ImportOption.DoNotIncludeJars.class})
class JUnit4DetectionTest {

  private static final List<Class<?>> excluded =
      Stream.of(
              WireMockClassRule.class,
              WireMockRule.class,
              WireMockStaticRule.class,
              JUnit4DetectionTest.class,
              WireMockJUnitRuleTest.class,
              WireMockRuleFailOnUnmatchedRequestsTest.class)
          .collect(toList());

  private static final DescribedPredicate<? super JavaClass> EXCLUDE_WIREMOCKJUNITRULETEST =
      describe(
          "exclude WireMockJUnitRuleTest",
          clazz ->
              excluded.stream().noneMatch(excl -> clazz.getName().contains(excl.getSimpleName())));

  private static final String REASON = "we want to migrate to JUnit Jupiter";

  @ArchTest
  static ArchRule junit4PackageShouldNotBeUsed =
      noClasses()
          .that(EXCLUDE_WIREMOCKJUNITRULETEST)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.junit")
          .as("org.junit should not be used")
          .because(REASON);

  @ArchTest
  static ArchRule junit4RunWithShouldNotBeUsed =
      classes()
          .that(EXCLUDE_WIREMOCKJUNITRULETEST)
          .should()
          .notBeAnnotatedWith(RunWith.class)
          .as("RunWith should not be used")
          .because(REASON);

  @ArchTest
  static ArchRule junit4ClassRuleShouldNotBeUsed =
      fields()
          .that()
          .areDeclaredInClassesThat(EXCLUDE_WIREMOCKJUNITRULETEST)
          .should()
          .notBeAnnotatedWith(ClassRule.class)
          .as("ClassRule should not be used")
          .because(REASON);

  @ArchTest
  static ArchRule junit4RuleShouldNotBeUsed =
      fields()
          .that()
          .areDeclaredInClassesThat(EXCLUDE_WIREMOCKJUNITRULETEST)
          .should()
          .notBeAnnotatedWith(Rule.class)
          .as("Rule should not be used")
          .because(REASON);
}
