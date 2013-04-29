/*
 * Copyright (C) 2013 Roger Abelenda
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

package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class LoggedRequestWithUrlMatcher extends TypeSafeMatcher<LoggedRequest> {

    private final String url;

    public LoggedRequestWithUrlMatcher(String url) {
        this.url = url;
    }

    @Override
    public boolean matchesSafely(LoggedRequest loggedRequest) {
        return loggedRequest.getUrl().equals(url);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A logged request with url: " + url);
    }
}
