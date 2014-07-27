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
package com.github.tomakehurst.wiremock.core;

import java.util.List;

import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.RequestDelaySpec;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.VerificationResult;

public interface Admin {

	void addStubMapping(StubMapping stubMapping);
    ListStubMappingsResult listAllStubMappings();
    void saveMappings();
	void resetMappings();
	void resetScenarios();
    void resetToDefaultMappings();
	VerificationResult countRequestsMatching(RequestPattern requestPattern);
    FindRequestsResult findRequestsMatching(RequestPattern requestPattern);
	void updateGlobalSettings(GlobalSettings settings);
    void addSocketAcceptDelay(RequestDelaySpec spec);
    void shutdownServer();
}
