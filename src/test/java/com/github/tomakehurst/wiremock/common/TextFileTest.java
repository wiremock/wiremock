/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class TextFileTest {
  @Test
  @DisabledOnOs(
      value = OS.WINDOWS,
      disabledReason = "This test can only be run on non-Windows " + "its behaviour is OS specific")
  public void returnsPathToFileOnLinuxSystems() throws Exception {
    TextFile textFile = new TextFile(new URI("file://home/bob/myfile.txt"));

    String path = textFile.getPath();

    assertEquals("/home/bob/myfile.txt", path);
  }

  @Test
  @EnabledOnOs(
      value = OS.WINDOWS,
      disabledReason =
          "This test can only be run on Windows "
              + "because File uses FileSystem in its constructor "
              + "and its behaviour is OS specific")
  public void returnsPathToFileOnWindowsSystems() throws Exception {
    TextFile textFile = new TextFile(new URI("file:/C:/Users/bob/myfile.txt"));

    Path path = Paths.get(textFile.getPath());

    assertEquals(Paths.get("C:/Users/bob/myfile.txt"), path);
  }
}
