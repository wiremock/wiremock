/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** A predicate to filter proxied ServeEvents against RequestPattern filters and IDs. */
public class ProxiedServeEventFilters implements Predicate<ServeEvent> {

  @JsonUnwrapped private RequestPattern filters;

  @JsonUnwrapped private List<UUID> ids;

  @JsonUnwrapped private boolean allowNonProxied;

  /** The constant ALLOW_ALL. */
  public static final ProxiedServeEventFilters ALLOW_ALL =
      new ProxiedServeEventFilters(null, null, false);

  /** Instantiates a new Proxied serve event filters. */
  // For Jackson. This class needs to be mutable as @JsonUnwrapped doesn't yet do constructor based
  // serialisation
  public ProxiedServeEventFilters() {}

  /**
   * Instantiates a new Proxied serve event filters.
   *
   * @param filters the filters
   * @param ids the ids
   * @param allowNonProxied the allow non proxied
   */
  public ProxiedServeEventFilters(RequestPattern filters, List<UUID> ids, boolean allowNonProxied) {
    this.filters = filters;
    this.ids = ids;
    this.allowNonProxied = allowNonProxied;
  }

  /**
   * Gets filters.
   *
   * @return the filters
   */
  public RequestPattern getFilters() {
    return filters;
  }

  /**
   * Sets filters.
   *
   * @param filters the filters
   */
  public void setFilters(RequestPattern filters) {
    this.filters = filters;
  }

  /**
   * Gets ids.
   *
   * @return the ids
   */
  public List<UUID> getIds() {
    return ids;
  }

  /**
   * Sets ids.
   *
   * @param ids the ids
   */
  public void setIds(List<UUID> ids) {
    this.ids = ids;
  }

  /**
   * Is allow non proxied boolean.
   *
   * @return the boolean
   */
  public boolean isAllowNonProxied() {
    return allowNonProxied;
  }

  /**
   * Sets allow non proxied.
   *
   * @param allowNonProxied the allow non proxied
   */
  public void setAllowNonProxied(boolean allowNonProxied) {
    this.allowNonProxied = allowNonProxied;
  }

  @Override
  public boolean test(ServeEvent serveEvent) {
    if (!serveEvent.getResponseDefinition().isProxyResponse() && !allowNonProxied) {
      return false;
    }

    if (filters != null && !filters.match(serveEvent.getRequest()).isExactMatch()) {
      return false;
    }

    return ids == null || ids.contains(serveEvent.getId());
  }
}
