/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin;

import static com.github.tomakehurst.wiremock.admin.RequestSpec.requestSpec;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;

import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.store.Stores;
import com.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.Map.Entry;

public class AdminRoutes {

  private final ImmutableBiMap<RequestSpec, AdminTask> routes;
  private final Iterable<AdminApiExtension> apiExtensions;
  private final Stores stores;
  private static final String MAPPINGS = "/mappings";
  private static final String FILES = "/files";
  private static final String SCENARIOS = "/scenarios";
  private static final String REQUESTS = "/requests";
  private static final String RECORDINGS = "/recordings";
  private static final String NEARMISSES = "/near-misses";
  private static final String SETTINGS = "/settings";
  private static final String DOCS = "/docs";

  public static AdminRoutes forClient() {
    return new AdminRoutes(Collections.emptyList(), null);
  }

  public static AdminRoutes forServer(Iterable<AdminApiExtension> apiExtensions, Stores stores) {
    return new AdminRoutes(apiExtensions, stores);
  }

  protected AdminRoutes(Iterable<AdminApiExtension> apiExtensions, Stores stores) {
    this.apiExtensions = apiExtensions;
    this.stores = stores;
    RouteBuilder routeBuilder = new RouteBuilder();
    initDefaultRoutes(routeBuilder);
    initAdditionalRoutes(routeBuilder);
    routes = routeBuilder.build();
  }

  private void initDefaultRoutes(Router router) {
    router.add(GET, "/", new RootTask());
    router.add(GET, "", new RootRedirectTask());
    router.add(POST, "/reset", new ResetTask());

    router.add(GET, MAPPINGS, new GetAllStubMappingsTask());
    router.add(POST, MAPPINGS, new CreateStubMappingTask());
    router.add(DELETE, MAPPINGS, new ResetStubMappingsTask());

    // Deprecated but kept so that 2.x client will still be compatible
    router.add(POST, MAPPINGS + "/edit", new OldEditStubMappingTask());

    router.add(POST, MAPPINGS + "/save", new SaveMappingsTask());
    router.add(POST, MAPPINGS + "/reset", new ResetToDefaultMappingsTask());
    router.add(GET, MAPPINGS + "/unmatched", new GetUnmatchedStubMappingsTask());
    router.add(DELETE, MAPPINGS + "/unmatched", new RemoveUnmatchedStubMappingsTask());
    router.add(GET, MAPPINGS + "/{id}", new GetStubMappingTask());
    router.add(PUT, MAPPINGS + "/{id}", new EditStubMappingTask());
    router.add(POST, MAPPINGS + "/remove", new RemoveMatchingStubMappingTask());
    router.add(DELETE, MAPPINGS + "/{id}", new RemoveStubMappingByIdTask());
    router.add(POST, MAPPINGS + "/find-by-metadata", new FindStubMappingsByMetadataTask());
    router.add(POST, MAPPINGS + "/remove-by-metadata", new RemoveStubMappingsByMetadataTask());
    router.add(POST, MAPPINGS + "/import", new ImportStubMappingsTask());

    router.add(GET, FILES, new GetAllStubFilesTask(stores));
    router.add(PUT, FILES + "/**", new EditStubFileTask(stores));
    router.add(DELETE, FILES + "/**", new DeleteStubFileTask(stores));
    router.add(GET, FILES + "/**", new GetStubFileTask(stores));

    router.add(GET, SCENARIOS, new GetAllScenariosTask());
    router.add(POST, SCENARIOS + "/reset", new ResetScenariosTask());
    router.add(PUT, SCENARIOS + "/{name}/state", new SetScenarioStateTask());

    router.add(GET, REQUESTS, new GetAllRequestsTask());
    router.add(DELETE, REQUESTS, new ResetRequestsTask());
    router.add(POST, REQUESTS + "/count", new GetRequestCountTask());
    router.add(POST, REQUESTS + "/find", new FindRequestsTask());
    router.add(GET, REQUESTS + "/unmatched", new FindUnmatchedRequestsTask());
    router.add(GET, REQUESTS + "/unmatched/near-misses", new FindNearMissesForUnmatchedTask());
    router.add(GET, REQUESTS + "/{id}", new GetServedStubTask());
    router.add(DELETE, REQUESTS + "/{id}", new RemoveServeEventTask());
    router.add(POST, REQUESTS + "/remove", new RemoveServeEventsByRequestPatternTask());
    router.add(POST, REQUESTS + "/remove-by-metadata", new RemoveServeEventsByStubMetadataTask());

    router.add(POST, RECORDINGS + "/snapshot", new SnapshotTask());
    router.add(POST, RECORDINGS + "/start", new StartRecordingTask());
    router.add(POST, RECORDINGS + "/stop", new StopRecordingTask());
    router.add(GET, RECORDINGS + "/status", new GetRecordingStatusTask());
    router.add(GET, "/recorder", new GetRecordingsIndexTask());

    router.add(POST, NEARMISSES + "/request", new FindNearMissesForRequestTask());
    router.add(POST, NEARMISSES + "/request-pattern", new FindNearMissesForRequestPatternTask());

    router.add(GET, SETTINGS, new GetGlobalSettingsTask());
    router.add(PUT, SETTINGS, new GlobalSettingsUpdateTask());
    router.add(POST, SETTINGS, new GlobalSettingsUpdateTask());
    router.add(PATCH, SETTINGS + "/extended", new PatchExtendedSettingsTask());

    router.add(POST, "/shutdown", new ShutdownServerTask());

    router.add(GET, DOCS + "/swagger", new GetSwaggerSpecTask());
    router.add(GET, DOCS, new GetDocIndexTask());

    router.add(GET, "/certs/wiremock-ca.crt", new GetCaCertTask());

    router.add(GET, "/health", new HealthCheckTask());

    router.add(GET, "/version", new GetVersionTask());
  }

  protected void initAdditionalRoutes(Router routeBuilder) {
    for (AdminApiExtension apiExtension : apiExtensions) {
      apiExtension.contributeAdminApiRoutes(routeBuilder);
    }
  }

  public AdminTask taskFor(final RequestMethod method, final String path) {
    return routes.entrySet().stream()
        .filter(entry -> entry.getKey().matches(method, path))
        .map(Entry::getValue)
        .findFirst()
        .orElseGet(NotFoundAdminTask::new);
  }

  public RequestSpec requestSpecForTask(final Class<? extends AdminTask> taskClass) {
    return routes.entrySet().stream()
        .filter(input -> input.getValue().getClass().equals(taskClass))
        .map(Entry::getKey)
        .findFirst()
        .orElseThrow(
            () ->
                new NotFoundException("No route could be found for " + taskClass.getSimpleName()));
  }

  protected static class RouteBuilder implements Router {
    private final ImmutableBiMap.Builder<RequestSpec, AdminTask> builder;

    public RouteBuilder() {
      builder = ImmutableBiMap.builder();
    }

    @Override
    public void add(
        RequestMethod method, String urlTemplate, Class<? extends AdminTask> taskClass) {
      try {
        AdminTask task = taskClass.getDeclaredConstructor().newInstance();
        add(requestSpec(method, urlTemplate), task);
      } catch (Exception e) {
        throwUnchecked(e);
      }
    }

    @Override
    public void add(RequestMethod method, String urlTemplate, AdminTask task) {
      add(requestSpec(method, urlTemplate), task);
    }

    public void add(RequestSpec requestSpec, AdminTask task) {
      builder.put(requestSpec, task);
    }

    ImmutableBiMap<RequestSpec, AdminTask> build() {
      return builder.build();
    }
  }
}
