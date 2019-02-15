package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.jknack.handlebars.io.TemplateSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

public class MappingBasedTemplateSource implements TemplateSource {

    public static MappingBasedTemplateSource fromMapping(StubMapping mapping) {
        return new MappingBasedTemplateSource(mapping.getId());
    }

    private UUID id;

    public MappingBasedTemplateSource(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String content(Charset charset) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String filename() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long lastModified() {
        throw new UnsupportedOperationException();
    }
}
