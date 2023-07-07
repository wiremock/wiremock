/*
 * Copyright (C) 2019-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class SystemKeyAuthorisorTest {

  @Test
  public void permitsAllowedKeys() {
    SystemKeyAuthoriser authoriser = new SystemKeyAuthoriser(Set.of("allowed_.*", "permitted_.*"));

    assertTrue(authoriser.isPermitted("allowed_key_1"));
    assertTrue(authoriser.isPermitted("ALLOWED_KEY_2"));
    assertTrue(authoriser.isPermitted("permitted_key_3"));
  }

  @Test
  public void forbidsNonAllowedKeys() {
    SystemKeyAuthoriser authoriser = new SystemKeyAuthoriser(Set.of("allowed_.*", "permitted_.*"));

    assertFalse(authoriser.isPermitted("forbidden_key_1"));
    assertFalse(authoriser.isPermitted("notallowed_key_2"));
    assertFalse(authoriser.isPermitted("notpermitted_key_3"));
  }

  @Test
  public void defaultsToWireMockPrefixIfNoPatternsSpecified() {
    SystemKeyAuthoriser authoriser = new SystemKeyAuthoriser(null);

    assertTrue(authoriser.isPermitted("wiremock_key_1"));
    assertTrue(authoriser.isPermitted("wiremock.thing.2"));
    assertFalse(authoriser.isPermitted("notallowed_key_2"));
  }
}
