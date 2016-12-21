/*
 * Copyright (C) 2016 Wojciech Bulaty
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
package com.github.tomakehurst.wiremock.core.mappings;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.stubbing.JsonFilesystemMappingsRepository;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.github.tomakehurst.wiremock.stubbing.StubMappingsWithTransformersAndCustomMatchers;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;

public class FilesystemStubMappingsFactory implements StubMappingsFactory {
    @Override
    public StubMappings create(Map<String, RequestMatcherExtension> customMatchers, Map<String, ResponseDefinitionTransformer> transformers, FileSource fileSource) {
        return new StubMappingsWithTransformersAndCustomMatchers(customMatchers,
                transformers,
                fileSource,
                new JsonFilesystemMappingsRepository(new JsonFileMappingsSource(fileSource.child(MAPPINGS_ROOT))));
    }
}
