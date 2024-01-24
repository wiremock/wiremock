/*
 * Copyright (C) 2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Lazy;
import java.util.function.Supplier;

public class LazyTemplateEngine extends TemplateEngine {
  private final Lazy<TemplateEngine> templateEngineLazy;

  public LazyTemplateEngine(Supplier<TemplateEngine> templateEngineSupplier) {
    this.templateEngineLazy = Lazy.lazy(templateEngineSupplier);
  }

  @Override
  public HandlebarsOptimizedTemplate getTemplate(Object key, String content) {
    return templateEngineLazy.get().getTemplate(key, content);
  }

  @Override
  public HandlebarsOptimizedTemplate getUncachedTemplate(String content) {
    return templateEngineLazy.get().getUncachedTemplate(content);
  }

  @Override
  public long getCacheSize() {
    return templateEngineLazy.get().getCacheSize();
  }

  @Override
  public void invalidateCache() {
    templateEngineLazy.get().invalidateCache();
  }

  @Override
  public Long getMaxCacheEntries() {
    return templateEngineLazy.get().getMaxCacheEntries();
  }
}
