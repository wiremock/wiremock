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
package com.github.tomakehurst.wiremock.common;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TextFileTest {
    @Test
    public void returnsPathToFileOnLinuxSystems() throws Exception {
        assumeFalse(SystemUtils.IS_OS_WINDOWS, "This test can only be run on non-Windows " +
            "its behaviour is OS specific");


        TextFile textFile = new TextFile(new URI("file://home/bob/myfile.txt"));

        String path = textFile.getPath();

        assertEquals("/home/bob/myfile.txt", path);
    }

    @Test
    public void returnsPathToFileOnWindowsSystems() throws Exception {
        assumeTrue(SystemUtils.IS_OS_WINDOWS, "This test can only be run on Windows " +
                "because File uses FileSystem in its constructor " +
                "and its behaviour is OS specific");

        TextFile textFile = new TextFile(new URI("file:/C:/Users/bob/myfile.txt"));

        Path path = Paths.get(textFile.getPath());

        assertEquals(Paths.get("C:/Users/bob/myfile.txt"), path);
    }
}