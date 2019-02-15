package com.github.tomakehurst.wiremock.extension.responsetemplating;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jknack.handlebars.Parser;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.cache.TemplateCache;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.github.tomakehurst.wiremock.http.CacheStrategy;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class DefinitionBasedTemplateCache implements TemplateCache {

    static class CacheEntry {
        private DefinitionBasedTemplateSource source;
        private long lastModified;
        private Template template;
        private Set<UUID> mappings = new HashSet<>();

        CacheEntry(DefinitionBasedTemplateSource source, CacheEntry existing) {
            this.source = source;
            this.lastModified = source.lastModified();
            if (existing != null) {
                this.mappings = new HashSet<>(existing.mappings);
            }
        }

        public DefinitionBasedTemplateSource getSource() {
            return source;
        }
        public Template getTemplate() {
            return template;
        }
        public boolean hasMappings() {
            return !mappings.isEmpty();
        }

        public CacheEntry setTemplate(Template template) {
            this.template = template;
            return this;
        }
        public boolean addMapping(UUID id) {
            return this.mappings.add(id);
        }
        public boolean removeMapping(UUID id) {
            return this.mappings.remove(id);
        }

        public boolean isOutdated() {
            return lastModified < source.lastModified();
        }
    }
    
    private HashMap<UUID                         , Set<DefinitionBasedTemplateSource>> mappings = new HashMap<>();
    private HashMap<DefinitionBasedTemplateSource, CacheEntry                        > entries  = new HashMap<>();
    private boolean reload = false;

    public Map<DefinitionBasedTemplateSource, Template> getCachedTemplates() {
        synchronized (mappings) {
            Map<DefinitionBasedTemplateSource, Template> templates = new HashMap<>(entries.size());
            for (CacheEntry entry : entries.values()) {
                templates.put(entry.getSource(), entry.getTemplate());
            }
            return templates;
        }
    }

    @Override
    public void clear() {
        synchronized (mappings) {
            mappings.clear();
            entries.clear();
        }
    }

    @Override
    public void evict(TemplateSource ref) {
        if (ref instanceof MappingBasedTemplateSource) {
            UUID id = ((MappingBasedTemplateSource)ref).getId();
            synchronized (mappings) {
                Set<DefinitionBasedTemplateSource> sources = mappings.remove(id);
                if (sources != null) {
                    for (DefinitionBasedTemplateSource source : sources) {
                        CacheEntry entry = entries.get(source);
                        entry.removeMapping(id);
                        if (!entry.hasMappings()) {
                            entries.remove(source);
                        }
                    }
                }
            }
            return;
        }
    }

    @Override
    public Template get(TemplateSource raw, Parser parser) throws IOException {
        if (raw instanceof DefinitionBasedTemplateSource && ((DefinitionBasedTemplateSource)raw).isCacheable()) {
            DefinitionBasedTemplateSource source = ((DefinitionBasedTemplateSource)raw);
            UUID id = source.getStubMappingId();
            CacheEntry entry;
            synchronized (mappings) {
                Set<DefinitionBasedTemplateSource> sources = mappings.get(id);
                if (sources == null) {
                    sources = new HashSet<>();
                    mappings.put(id, sources);
                }
                sources.add(source);
                entry = entries.get(source);
                if (entry == null || entry.isOutdated()) {
                    entry = new CacheEntry(source, entry);
                    entries.put(source, entry);
                }
                entry.addMapping(id);
            }
            synchronized (entry) {
                if (entry.getTemplate() == null) {
                    entry.setTemplate(parser.parse(source));
                }
            }
            return entry.getTemplate();
        }
        return parser.parse(raw);
    }

    @Override
    public DefinitionBasedTemplateCache setReload(boolean reload) {
        this.reload = reload;
        return this;
    }

}
