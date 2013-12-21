package com.github.tomakehurst.wiremock.standalone;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.StubMappings;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.sun.istack.internal.Nullable;

import java.util.Collection;

import static com.github.tomakehurst.wiremock.common.Json.write;

public class JsonFileMappingsSaver implements MappingsSaver {
    private final FileSource mappingsFileSource;
    private final VeryShortIdGenerator idGenerator;

    public JsonFileMappingsSaver(FileSource mappingsFileSource) {
        this.mappingsFileSource = mappingsFileSource;
        idGenerator = new VeryShortIdGenerator();
    }

    public void saveMappings(StubMappings stubMappings) {
        Collection<StubMapping> transientStubs = Collections2.filter(stubMappings.getAll(), new Predicate<StubMapping>() {
            @Override
            public boolean apply(@Nullable StubMapping input) {
                return input != null && input.isTransient();
            }
        });

        for (StubMapping mapping : transientStubs) {
            String fileId = idGenerator.generate();
            String mappingFileName = "saved-mapping-" + fileId + ".json";
            mappingsFileSource.writeTextFile(mappingFileName, write(mapping));
            mapping.setTransient(false);
        }
    }
}
