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
package com.github.tomakehurst.wiremock.verification;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Optional;

import java.util.List;
import java.util.UUID;

public class DisabledRequestJournal implements RequestJournal {

    @Override
    public int countRequestsMatching(RequestPattern requestPattern) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public List<ServeEvent> getAllServeEvents() {
        throw new RequestJournalDisabledException();
    }

    @Override
    public Optional<ServeEvent> getServeEvent(UUID id) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public void reset() {
    }

    @Override
    public void requestReceived(ServeEvent serveEvent) {
    }

    @Override
    public void removeEvent(UUID eventId) {
    }

    @Override
    public List<ServeEvent> removeEventsMatching(RequestPattern requestPattern) {
        throw new RequestJournalDisabledException();
    }

    @Override
    public List<ServeEvent> removeServeEventsForStubsMatchingMetadata(StringValuePattern metadataPattern) {
        throw new RequestJournalDisabledException();
    }
}
