package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.NumberHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.ParameterNormalisingHelperWrapper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.SystemValueHelper;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.WireMockHelpers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class TemplateEngine {

    private final Handlebars handlebars;
    private final Cache<Object, HandlebarsOptimizedTemplate> cache;
    private final Long maxCacheEntries;

    public TemplateEngine(Map<String, Helper<?>> helpers, Long maxCacheEntries, Set<String> permittedSystemKeys) {
        this(new Handlebars(), helpers, maxCacheEntries, permittedSystemKeys);
    }

    public TemplateEngine(Handlebars handlebars, Map<String, Helper<?>> helpers, Long maxCacheEntries, Set<String> permittedSystemKeys) {
        this.handlebars = handlebars;
        this.maxCacheEntries = maxCacheEntries;
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if (maxCacheEntries != null) {
            cacheBuilder.maximumSize(maxCacheEntries);
        }
        cache = cacheBuilder.build();

        addHelpers(helpers, permittedSystemKeys);
        decorateHelpersWithParameterUnwrapper();
    }

    private void addHelpers(Map<String, Helper<?>> helpers, Set<String> permittedSystemKeys) {
        for (StringHelpers helper: StringHelpers.values()) {
            if (!helper.name().equals("now")) {
                this.handlebars.registerHelper(helper.name(), helper);
            }
        }

        for (NumberHelper helper: NumberHelper.values()) {
            this.handlebars.registerHelper(helper.name(), helper);
        }

        for (ConditionalHelpers helper: ConditionalHelpers.values()) {
            this.handlebars.registerHelper(helper.name(), helper);
        }

        this.handlebars.registerHelper(AssignHelper.NAME, new AssignHelper());

        //Add all available wiremock helpers
        for (WireMockHelpers helper: WireMockHelpers.values()) {
            this.handlebars.registerHelper(helper.name(), helper);
        }

        this.handlebars.registerHelper("systemValue", new SystemValueHelper(new SystemKeyAuthoriser(permittedSystemKeys)));

        for (Map.Entry<String, Helper<?>> entry: helpers.entrySet()) {
            this.handlebars.registerHelper(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void decorateHelpersWithParameterUnwrapper() {
        handlebars.helpers().forEach(entry -> {
            Helper<?> newHelper = new ParameterNormalisingHelperWrapper((Helper<Object>) entry.getValue());
            handlebars.registerHelper(entry.getKey(), newHelper);
        });
    }

    public HandlebarsOptimizedTemplate getTemplate(final Object key, final String content) {
        if (maxCacheEntries != null && maxCacheEntries < 1) {
            return getUncachedTemplate(content);
        }

        try {
            return cache.get(key, () -> new HandlebarsOptimizedTemplate(handlebars, content));
        } catch (ExecutionException e) {
            return Exceptions.throwUnchecked(e, HandlebarsOptimizedTemplate.class);
        }
    }

    public HandlebarsOptimizedTemplate getUncachedTemplate(final String content) {
        return new HandlebarsOptimizedTemplate(handlebars, content);
    }

    public long getCacheSize() {
        return cache.size();
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }

    public Long getMaxCacheEntries() {
        return maxCacheEntries;
    }
}
