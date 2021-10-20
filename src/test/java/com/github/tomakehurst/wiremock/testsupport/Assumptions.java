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
package com.github.tomakehurst.wiremock.testsupport;

import org.apache.commons.lang3.SystemUtils;

import static org.junit.Assume.assumeFalse;

public class Assumptions {

    public static void doNotRunOnMacOSXInCI() {
        assumeFalse(SystemUtils.IS_OS_MAC_OSX && "true".equalsIgnoreCase(System.getenv("CI")));
    }
}
