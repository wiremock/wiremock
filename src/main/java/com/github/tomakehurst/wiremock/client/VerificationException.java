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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.List;

public class VerificationException extends AssertionError {

	private static final long serialVersionUID = 5116216532516117538L;

	public VerificationException(String message) {
		super(message);
	}

    public VerificationException(RequestPattern expected, List<LoggedRequest> requests) {
        super(String.format("Expected at least one request matching: %s\nRequests received: %s",
                expected.toString(), Json.write(requests)));
    }

    public VerificationException(RequestPattern expected, int expectedCount, List<LoggedRequest> requests) {
        super(String.format("Expected exactly %d requests matching: %s\nRequests received: %s",
                expectedCount, expected.toString(), Json.write(requests)));
    }
}
