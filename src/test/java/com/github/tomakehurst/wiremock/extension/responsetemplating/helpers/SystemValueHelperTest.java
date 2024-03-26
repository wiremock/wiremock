/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static org.junit.jupiter.api.Assertions.*;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.SystemKeyAuthoriser;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

public class SystemValueHelperTest {

  private SystemValueHelper helper;

  @BeforeEach
  public void init() {
    helper = new SystemValueHelper(new SystemKeyAuthoriser(Set.of(".*")));
    LocalNotifier.set(new ConsoleNotifier(true));
  }

  @Test
  public void getExistingEnvironmentVariableShouldNotNull() {
    Optional<String> key = System.getenv().keySet().stream().findFirst();
    Assumptions.assumeTrue(key.isPresent());
    Map<String, Object> optionsHash = Map.of("key", key.get(), "type", "ENVIRONMENT");

    String output = render(optionsHash);
    assertEquals(System.getenv(key.get()), output);
  }

  @Test
  public void getExistingEnvironmentVariableWithDefault() {
    Optional<String> key = System.getenv().keySet().stream().findFirst();
    Assumptions.assumeTrue(key.isPresent());
    Map<String, Object> optionsHash =
        Map.of("key", key.get(), "type", "ENVIRONMENT", "default", "DEFAULT");

    String output = render(optionsHash);
    assertEquals(System.getenv(key.get()), output);
  }

  @Test
  public void getNonExistingEnvironmentVariableShouldNull() {
    Map<String, Object> optionsHash = Map.of("key", "NON_EXISTING_VAR", "type", "ENVIRONMENT");

    String output = render(optionsHash);
    assertNull(output);
  }

  @Test
  public void getNonExistingEnvironmentVariableWithDefault() {
    Map<String, Object> optionsHash =
        Map.of("key", "NON_EXISTING_VAR", "type", "ENVIRONMENT", "default", "DEFAULT");

    String output = render(optionsHash);
    assertEquals("DEFAULT", output);
  }

  @Test
  public void getForbiddenEnvironmentVariableShouldReturnError() {
    helper = new SystemValueHelper(new SystemKeyAuthoriser(Set.of("JAVA*")));

    Map<String, Object> optionsHash = Map.of("key", "TEST_VAR", "type", "ENVIRONMENT");
    String value = render(optionsHash);
    assertEquals("[ERROR: Access to TEST_VAR is denied]", value);
  }

  @Test
  public void getEmptyKeyShouldReturnError() {
    Map<String, Object> optionsHash = Map.of("key", "", "type", "PROPERTY");
    String value = render(optionsHash);
    assertEquals("[ERROR: The key cannot be empty]", value);
  }

  @Test
  @ClearSystemProperty(key = "test.key")
  public void getAllowedPropertyShouldSuccess() {
    helper = new SystemValueHelper(new SystemKeyAuthoriser(Set.of("test.*")));
    System.setProperty("test.key", "aaa");
    assertEquals("aaa", System.getProperty("test.key"));
    Map<String, Object> optionsHash = Map.of("key", "test.key", "type", "PROPERTY");
    String value = render(optionsHash);
    assertEquals("aaa", value);
  }

  @Test
  @ClearSystemProperty(key = "test.key")
  public void getAllowedPropertyWithDefault() {
    helper = new SystemValueHelper(new SystemKeyAuthoriser(Set.of("test.*")));
    System.setProperty("test.key", "aaa");
    assertEquals("aaa", System.getProperty("test.key"));
    Map<String, Object> optionsHash =
        Map.of("key", "test.key", "type", "PROPERTY", "default", "DEFAULT");
    String value = render(optionsHash);
    assertEquals("aaa", value);
  }

  @Test
  @ClearSystemProperty(key = "test.key")
  public void getForbiddenPropertyShouldReturnError() {
    helper = new SystemValueHelper(new SystemKeyAuthoriser(Set.of("JAVA.*")));
    System.setProperty("test.key", "aaa");
    Map<String, Object> optionsHash = Map.of("key", "test.key", "type", "PROPERTY");
    String value = render(optionsHash);
    assertEquals("[ERROR: Access to test.key is denied]", value);
  }

  @Test
  public void getNonExistingSystemPropertyShouldNull() {
    Map<String, Object> optionsHash = Map.of("key", "not.existing.prop", "type", "PROPERTY");
    String output = render(optionsHash);
    assertNull(output);
  }

  @Test
  public void getNonExistingSystemPropertyWithDefault() {
    Map<String, Object> optionsHash =
        Map.of("key", "not.existing.prop", "type", "PROPERTY", "default", "DEFAULT");
    String output = render(optionsHash);
    assertEquals("DEFAULT", output);
  }

  @Test
  public void getDefaultType() {
    Optional<String> key = System.getenv().keySet().stream().findFirst();
    Assumptions.assumeTrue(key.isPresent());
    Map<String, Object> optionsHash = Map.of("key", key.get());

    String output = render(optionsHash);
    assertEquals(System.getenv(key.get()), output);
  }

  private String render(Map<String, Object> optionsHash) {
    return helper.apply(
        null, new Options.Builder(null, null, null, null, null).setHash(optionsHash).build());
  }
}
