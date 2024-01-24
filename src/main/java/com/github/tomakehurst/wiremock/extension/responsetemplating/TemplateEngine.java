/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import static java.util.Collections.emptyMap;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.helper.AssignHelper;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.NumberHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.tomakehurst.wiremock.common.Exceptions;
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

  public static TemplateEngine defaultTemplateEngine() {
    return new TemplateEngine(emptyMap(), null, null, false);
  }

  public TemplateEngine(
      Map<String, Helper<?>> helpers,
      Long maxCacheEntries,
      Set<String> permittedSystemKeys,
      boolean escapingDisabled) {

    this.handlebars =
        escapingDisabled ? new Handlebars().with(EscapingStrategy.NOOP) : new Handlebars();

    this.maxCacheEntries = maxCacheEntries;
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    if (maxCacheEntries != null) {
      cacheBuilder.maximumSize(maxCacheEntries);
    }
    cache = cacheBuilder.build();

    addHelpers(helpers, permittedSystemKeys);
  }

  protected TemplateEngine() {
    this.handlebars = null;
    this.maxCacheEntries = null;
    this.cache = null;
  }

  private void addHelpers(Map<String, Helper<?>> helpers, Set<String> permittedSystemKeys) {
    for (StringHelpers helper : StringHelpers.values()) {
      if (!helper.name().equals("now")) {
        this.handlebars.registerHelper(helper.name(), helper);
      }
    }

    for (NumberHelper helper : NumberHelper.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    for (ConditionalHelpers helper : ConditionalHelpers.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    this.handlebars.registerHelper(AssignHelper.NAME, new AssignHelper());

    // Add all available wiremock helpers
    for (WireMockHelpers helper : WireMockHelpers.values()) {
      this.handlebars.registerHelper(helper.name(), helper);
    }

    this.handlebars.registerHelper(
        "systemValue", new SystemValueHelper(new SystemKeyAuthoriser(permittedSystemKeys)));

    for (Map.Entry<String, Helper<?>> entry : helpers.entrySet()) {
      this.handlebars.registerHelper(entry.getKey(), entry.getValue());
    }
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
