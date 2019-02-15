package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import com.github.jknack.handlebars.io.ReloadableTemplateSource;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.http.CacheStrategy;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.lang.String.format;

public abstract class DefinitionBasedTemplateSource implements TemplateSource {

    private UUID stubMappingId;
    private CacheStrategy cacheStrategy;
    private String filename;
    private String id;

    public static class BodyFile extends DefinitionBasedTemplateSource {

        public BodyFile(UUID stubMappingId, CacheStrategy cacheStrategy, String path) {
            super(stubMappingId, cacheStrategy, path, path);
        }


        @Override
        public String content(Charset charset) throws IOException {
            return new String(Files.readAllBytes(Paths.get(getId())), charset);
        }

        @Override
        public long lastModified() {
            try {
                return Files.getLastModifiedTime(Paths.get(getId())).toMillis();
            } catch (IOException e) {
                throwUnchecked(e);
                return 0;
            }
        }
    }

    public static class BodyContent extends DefinitionBasedTemplateSource {
        public BodyContent(UUID stubMappingId, CacheStrategy cacheStrategy, String content) {
            super(stubMappingId, cacheStrategy, content, format("inline@%h", content));
        }

        @Override
        public String content(Charset charset) {
            return getId();
        }

        @Override
        public long lastModified() {
            return 0;
        }
    }

    public static DefinitionBasedTemplateSource fromString(UUID stubMappingId, String content, CacheStrategy cacheStrategy) {
        return new BodyContent(stubMappingId, cacheStrategy, content);
    }

    public static DefinitionBasedTemplateSource fromTextFile(UUID stubMappingId, TextFile textfile, CacheStrategy cacheStrategy) {
        return new BodyFile(stubMappingId, cacheStrategy, textfile.getPath());
    }

    private DefinitionBasedTemplateSource(UUID stubMappingId, CacheStrategy cacheStrategy, String id, String filename) {
        this.stubMappingId = stubMappingId;
        this.cacheStrategy = cacheStrategy;
        this.id            = id;
        this.filename      = filename;
    }

    @Override
    public String filename() {
        return filename;
    }

    public UUID getStubMappingId() {
        return stubMappingId;
    }

    public CacheStrategy getCacheStrategy() {
        return this.cacheStrategy;
    }

    public String getId() {
        return id;
    }

    public boolean isCacheable() {
        return !CacheStrategy.isNever(getCacheStrategy());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !this.getClass().isAssignableFrom(obj.getClass())) return false;

        DefinitionBasedTemplateSource that = (DefinitionBasedTemplateSource) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return format("%s(%s)", getClass().getSimpleName(), getId());
    }
}
