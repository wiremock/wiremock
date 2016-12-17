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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;

import java.util.ArrayList;
import java.util.List;

public class JsonFilesystemMappingsRepository implements MappingsRepository {
    private final JsonFileMappingsSource source;
    private StubMapping proxy;

    public JsonFilesystemMappingsRepository(JsonFileMappingsSource source) {
        this.source = source;
    }

    @Override
    public Iterable<StubMapping> getAll() {
        final List<StubMapping> stubMappings = new ArrayList<>();
        source.loadMappingsInto(new StubMappingsCollector() {
            @Override
            public void addMapping(StubMapping mapping) {
                stubMappings.add(mapping);
            }
        });
        if (proxy != null) {
            stubMappings.add(proxy);
        }
        return stubMappings;
    }

    @Override
    public void add(StubMapping mapping) {
        if(!mapping.getResponse().isProxyResponse()) {
            source.save(mapping);
        } else {
            proxy = mapping;
        }
    }

    @Override
    public void remove(StubMapping mapping) {
        source.remove(mapping);
    }

    @Override
    public void replace(StubMapping existingMapping, StubMapping stubMapping) {
        remove(existingMapping);
        add(stubMapping);
    }

    @Override
    public void clear() {
        source.removeAll();
        proxy = null;
    }
}
