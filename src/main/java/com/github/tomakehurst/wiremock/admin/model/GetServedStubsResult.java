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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Paginator;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;

import java.util.List;

public class GetServedStubsResult extends RequestJournalDependentResult<ServedStub> {

    @JsonCreator
    public GetServedStubsResult(@JsonProperty("servedStubs") List<ServedStub> source,
                                @JsonProperty("meta") Meta meta,
                                @JsonProperty("requestJournalDisabled") boolean requestJournalDisabled) {
        super(source, meta, requestJournalDisabled);
    }

    public GetServedStubsResult(Paginator<ServedStub> paginator, boolean requestJournalDisabled) {
        super(paginator, requestJournalDisabled);
    }

    public static GetServedStubsResult requestJournalEnabled(Paginator<ServedStub> paginator) {
        return new GetServedStubsResult(paginator, false);
    }

    public static GetServedStubsResult requestJournalDisabled(Paginator<ServedStub> paginator) {
        return new GetServedStubsResult(paginator, true);
    }

    public List<ServedStub> getServedStubs() {
        return select();
    }
}
