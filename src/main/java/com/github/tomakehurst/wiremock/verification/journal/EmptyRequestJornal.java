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

package com.github.tomakehurst.wiremock.verification.journal;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.Collections;
import java.util.List;

public class EmptyRequestJornal implements RequestJournal {

    @Override
    public int countRequestsMatching(RequestPattern requestPattern) {
        return 0;
    }

    @Override
    public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
        return Collections.emptyList();
    }

    @Override
    public void reset() {}

    @Override
    public void requestReceived(Request request, Response response) {}

}
