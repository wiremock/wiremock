/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.HttpAdminClient;
import com.github.tomakehurst.wiremock.common.HttpClientUtils;
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
class HttpClientTest {

  @ArchTest
  static ArchRule httpClientShouldNotLeak =
      noClasses()
          .that()
          .resideOutsideOfPackage("..http..")
          .and()
          .areNotAssignableTo(HttpAdminClient.class)
          .and()
          .areNotAssignableTo(HttpClientUtils.class)
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.apache.hc..")
          .as("Apache HttpClient should be limited to http package")
          .because("we want to make the third party dependency optional");
}
