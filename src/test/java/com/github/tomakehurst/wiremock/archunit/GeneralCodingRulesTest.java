/*
 * Copyright (C) 2021-2023 Thomas Akehurst
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

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packagesOf = WireMockServer.class,
    importOptions = {
      ImportOption.DoNotIncludeArchives.class,
      ImportOption.DoNotIncludeJars.class,
      ImportOption.DoNotIncludeTests.class
    })
class GeneralCodingRulesTest {

  @ArchTest
  static ArchRule RULE_NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS =
      noClasses()
          .that()
          .areNotAssignableTo(ConsoleNotifier.class)
          .and()
          .areNotAssignableTo(WireMockServerRunner.class)
          .should(ACCESS_STANDARD_STREAMS)
          .as("classes should not access standard streams");

  @ArchTest
  static ArchRule RULE_NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS =
      freeze(NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS);

  @ArchTest
  static ArchRule RULE_NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING =
      NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

  @ArchTest static ArchRule RULE_NO_CLASSES_SHOULD_USE_JODATIME = NO_CLASSES_SHOULD_USE_JODATIME;
}
