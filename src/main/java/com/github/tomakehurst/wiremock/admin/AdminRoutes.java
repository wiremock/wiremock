/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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
import static com.google.common.collect.Iterables.tryFind;

import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.verification.notmatched.PlainTextStubNotMatchedRenderer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.Map;

public class AdminRoutes {

  private final ImmutableBiMap<RequestSpec, AdminTask> routes;
  private final Iterable<AdminApiExtension> apiExtensions;
  private final Stores stores;

  public static AdminRoutes forClient() {
    return new AdminRoutes(
        Collections.<AdminApiExtension>emptyList(), new PlainTextStubNotMatchedRenderer(), null);
  }

  public static AdminRoutes forServer(
      Iterable<AdminApiExtension> apiExtensions, AdminTask notMatchedTask, Stores stores) {
    return new AdminRoutes(apiExtensions, notMatchedTask, stores);
  }

  protected AdminRoutes(
      Iterable<AdminApiExtension> apiExtensions, AdminTask notMatchedTask, Stores stores) {
    this.apiExtensions = apiExtensions;
    this.stores = stores;
    RouteBuilder routeBuilder = new RouteBuilder();
    initDefaultRoutes(routeBuilder);
    initAdditionalRoutes(routeBuilder);
    routeBuilder.add(ANY, "/not-matched", notMatchedTask);
    routes = routeBuilder.build();
  }

  private void initDefaultRoutes(Router router) {
    router.add(GET, "/", new RootTask());
    router.add(GET, "", new RootRedirectTask());
    router.add(POST, "/reset", new ResetTask());

    router.add(GET, "/mappings", new GetAllStubMappingsTask());
    router.add(POST, "/mappings", new CreateStubMappingTask());
    router.add(DELETE, "/mappings", new ResetStubMappingsTask());

    router.add(POST, "/mappings/new", new OldCreateStubMappingTask()); // Deprecated
    router.add(POST, "/mappings/remove", new OldRemoveStubMappingTask()); // Deprecated
    router.add(POST, "/mappings/edit", new OldEditStubMappingTask()); // Deprecated
    router.add(POST, "/mappings/save", new SaveMappingsTask());
    router.add(POST, "/mappings/reset", new ResetToDefaultMappingsTask());
    router.add(GET, "/mappings/{id}", new GetStubMappingTask());
    router.add(PUT, "/mappings/{id}", new EditStubMappingTask());
    router.add(DELETE, "/mappings/{id}", new RemoveStubMappingTask());
    router.add(POST, "/mappings/find-by-metadata", new FindStubMappingsByMetadataTask());
    router.add(POST, "/mappings/remove-by-metadata", new RemoveStubMappingsByMetadataTask());
    router.add(POST, "/mappings/import", new ImportStubMappingsTask());

    router.add(GET, "/files", new GetAllStubFilesTask(stores));
    router.add(PUT, "/files/**", new EditStubFileTask(stores));
    router.add(DELETE, "/files/**", new DeleteStubFileTask(stores));

    router.add(GET, "/scenarios", new GetAllScenariosTask());
    router.add(POST, "/scenarios/reset", new ResetScenariosTask());
    router.add(PUT, "/scenarios/{name}/state", new SetScenarioStateTask());

    router.add(GET, "/requests", new GetAllRequestsTask());
    router.add(DELETE, "/requests", new ResetRequestsTask());
    router.add(POST, "/requests/reset", new OldResetRequestsTask()); // Deprecated
    router.add(POST, "/requests/count", new GetRequestCountTask());
    router.add(POST, "/requests/find", new FindRequestsTask());
    router.add(GET, "/requests/unmatched", new FindUnmatchedRequestsTask());
    router.add(GET, "/requests/unmatched/near-misses", new FindNearMissesForUnmatchedTask());
    router.add(GET, "/requests/{id}", new GetServedStubTask());
    router.add(DELETE, "/requests/{id}", new RemoveServeEventTask());
    router.add(POST, "/requests/remove", new RemoveServeEventsByRequestPatternTask());
    router.add(POST, "/requests/remove-by-metadata", new RemoveServeEventsByStubMetadataTask());

    router.add(POST, "/recordings/snapshot", new SnapshotTask());
    router.add(POST, "/recordings/start", new StartRecordingTask());
    router.add(POST, "/recordings/stop", new StopRecordingTask());
    router.add(GET, "/recordings/status", new GetRecordingStatusTask());
    router.add(GET, "/recorder", new GetRecordingsIndexTask());

    router.add(POST, "/near-misses/request", new FindNearMissesForRequestTask());
    router.add(POST, "/near-misses/request-pattern", new FindNearMissesForRequestPatternTask());

    router.add(GET, "/settings", new GetGlobalSettingsTask());
    router.add(PUT, "/settings", new GlobalSettingsUpdateTask());
    router.add(POST, "/settings", new GlobalSettingsUpdateTask());
    router.add(PATCH, "/settings/extended", new PatchExtendedSettingsTask());

    router.add(POST, "/shutdown", new ShutdownServerTask());

    router.add(GET, "/docs/swagger", new GetSwaggerSpecTask());
    router.add(GET, "/docs", new GetDocIndexTask());

    router.add(GET, "/certs/wiremock-ca.crt", new GetCaCertTask());
  }

  protected void initAdditionalRoutes(Router routeBuilder) {
    for (AdminApiExtension apiExtension : apiExtensions) {
      apiExtension.contributeAdminApiRoutes(routeBuilder);
    }
  }

  public AdminTask taskFor(final RequestMethod method, final String path) {
    return tryFind(
            routes.entrySet(),
            new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
              @Override
              public boolean apply(Map.Entry<RequestSpec, AdminTask> entry) {
                return entry.getKey().matches(method, path);
              }
            })
        .transform(
            new Function<Map.Entry<RequestSpec, AdminTask>, AdminTask>() {
              @Override
              public AdminTask apply(Map.Entry<RequestSpec, AdminTask> input) {
                return input.getValue();
              }
            })
        .or(new NotFoundAdminTask());
  }

  public RequestSpec requestSpecForTask(final Class<? extends AdminTask> taskClass) {
    RequestSpec requestSpec =
        tryFind(
                routes.entrySet(),
                new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
                  @Override
                  public boolean apply(Map.Entry<RequestSpec, AdminTask> input) {
                    return input.getValue().getClass().equals(taskClass);
                  }
                })
            .transform(
                new Function<Map.Entry<RequestSpec, AdminTask>, RequestSpec>() {
                  @Override
                  public RequestSpec apply(Map.Entry<RequestSpec, AdminTask> input) {
                    return input.getKey();
                  }
                })
            .orNull();

    if (requestSpec == null) {
      throw new NotFoundException("No route could be found for " + taskClass.getSimpleName());
    }

    return requestSpec;
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
