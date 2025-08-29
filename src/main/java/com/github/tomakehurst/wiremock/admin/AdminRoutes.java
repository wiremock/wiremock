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
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PATCH;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;

import com.github.tomakehurst.wiremock.admin.tasks.CreateStubMappingTask;
import com.github.tomakehurst.wiremock.admin.tasks.DeleteStubFileTask;
import com.github.tomakehurst.wiremock.admin.tasks.EditStubFileTask;
import com.github.tomakehurst.wiremock.admin.tasks.EditStubMappingTask;
import com.github.tomakehurst.wiremock.admin.tasks.FindNearMissesForRequestPatternTask;
import com.github.tomakehurst.wiremock.admin.tasks.FindNearMissesForRequestTask;
import com.github.tomakehurst.wiremock.admin.tasks.FindNearMissesForUnmatchedTask;
import com.github.tomakehurst.wiremock.admin.tasks.FindRequestsTask;
import com.github.tomakehurst.wiremock.admin.tasks.FindUnmatchedRequestsTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetAllRequestsTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetAllStubFilesTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetAllStubMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetCaCertTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetDocIndexTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetRecordingsIndexTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetRequestCountTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetServedStubTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetStubFileTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetStubMappingTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetSwaggerSpecTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetUnmatchedStubMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.GetVersionTask;
import com.github.tomakehurst.wiremock.admin.tasks.GlobalSettingsUpdateTask;
import com.github.tomakehurst.wiremock.admin.tasks.HealthCheckTask;
import com.github.tomakehurst.wiremock.admin.tasks.NotFoundAdminTask;
import com.github.tomakehurst.wiremock.admin.tasks.OldEditStubMappingTask;
import com.github.tomakehurst.wiremock.admin.tasks.RemoveMatchingStubMappingTask;
import com.github.tomakehurst.wiremock.admin.tasks.RemoveStubMappingByIdTask;
import com.github.tomakehurst.wiremock.admin.tasks.RemoveUnmatchedStubMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.ResetRequestsTask;
import com.github.tomakehurst.wiremock.admin.tasks.ResetScenariosTask;
import com.github.tomakehurst.wiremock.admin.tasks.ResetStubMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.ResetTask;
import com.github.tomakehurst.wiremock.admin.tasks.ResetToDefaultMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.RootRedirectTask;
import com.github.tomakehurst.wiremock.admin.tasks.RootTask;
import com.github.tomakehurst.wiremock.admin.tasks.SaveMappingsTask;
import com.github.tomakehurst.wiremock.admin.tasks.ShutdownServerTask;
import com.github.tomakehurst.wiremock.admin.tasks.SnapshotTask;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.store.Stores;
import com.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Manages the registration and lookup of admin API routes.
 *
 * <p>This class holds the mapping between a {@link RequestSpec} (representing an HTTP method and
 * URL path) and an {@link AdminTask} that performs an administrative action. It supports finding a
 * task for a given request and, conversely, finding the request spec for a given task class.
 *
 * <p>Routes can be extended via the {@link AdminApiExtension} mechanism.
 *
 * @see AdminTask
 * @see RequestSpec
 * @see AdminApiExtension
 */
public class AdminRoutes {

  private final ImmutableBiMap<RequestSpec, AdminTask> routes;
  private final Iterable<AdminApiExtension> apiExtensions;
  private final Stores stores;

  /**
   * Creates a new {@code AdminRoutes} instance for client-side usage.
   *
   * <p>Client-side routes do not require extensions or stores as they are typically used for
   * building requests to the server, not for executing tasks.
   *
   * @return A new {@code AdminRoutes} instance for client-side purposes.
   */
  public static AdminRoutes forClient() {
    return new AdminRoutes(Collections.emptyList(), null);
  }

  /**
   * Creates a new {@code AdminRoutes} instance for server-side usage.
   *
   * <p>Server-side routes are initialized with all default WireMock admin tasks and can be
   * augmented with custom tasks from extensions.
   *
   * @param apiExtensions An iterable of {@link AdminApiExtension} instances to contribute custom
   *     routes.
   * @param stores The {@link Stores} instance for tasks that need to interact with storage.
   * @return A new, fully configured {@code AdminRoutes} instance for server-side use.
   */
  public static AdminRoutes forServer(Iterable<AdminApiExtension> apiExtensions, Stores stores) {
    return new AdminRoutes(apiExtensions, stores);
  }

  /**
   * Constructs an AdminRoutes instance, initializing default and extension-provided routes.
   *
   * @param apiExtensions Extensions that contribute additional routes.
   * @param stores The stores instance for tasks requiring data access.
   */
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

    router.add(GET, "/mappings", new GetAllStubMappingsTask());
    router.add(POST, "/mappings", new CreateStubMappingTask());
    router.add(DELETE, "/mappings", new ResetStubMappingsTask());

    // Deprecated but kept so that 2.x client will still be compatible
    router.add(POST, "/mappings/edit", new OldEditStubMappingTask());

    router.add(POST, "/mappings/save", new SaveMappingsTask());
    router.add(POST, "/mappings/reset", new ResetToDefaultMappingsTask());
    router.add(GET, "/mappings/unmatched", new GetUnmatchedStubMappingsTask());
    router.add(DELETE, "/mappings/unmatched", new RemoveUnmatchedStubMappingsTask());
    router.add(GET, "/mappings/{id}", new GetStubMappingTask());
    router.add(PUT, "/mappings/{id}", new EditStubMappingTask());
    router.add(POST, "/mappings/remove", new RemoveMatchingStubMappingTask());
    router.add(DELETE, "/mappings/{id}", new RemoveStubMappingByIdTask());
    router.add(POST, "/mappings/find-by-metadata", new FindStubMappingsByMetadataTask());
    router.add(POST, "/mappings/remove-by-metadata", new RemoveStubMappingsByMetadataTask());
    router.add(POST, "/mappings/import", new ImportStubMappingsTask());

    router.add(GET, "/files", new GetAllStubFilesTask(stores));
    router.add(PUT, "/files/**", new EditStubFileTask(stores));
    router.add(DELETE, "/files/**", new DeleteStubFileTask(stores));
    router.add(GET, "/files/**", new GetStubFileTask(stores));

    router.add(GET, "/scenarios", new GetAllScenariosTask());
    router.add(POST, "/scenarios/reset", new ResetScenariosTask());
    router.add(PUT, "/scenarios/{name}/state", new SetScenarioStateTask());

    router.add(GET, "/requests", new GetAllRequestsTask());
    router.add(DELETE, "/requests", new ResetRequestsTask());
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

    router.add(GET, "/health", new HealthCheckTask());

    router.add(GET, "/version", new GetVersionTask());
  }

  /**
   * Initializes additional routes contributed by {@link AdminApiExtension} implementations.
   *
   * @param routeBuilder The {@link Router} to add the custom routes to.
   */
  protected void initAdditionalRoutes(Router routeBuilder) {
    for (AdminApiExtension apiExtension : apiExtensions) {
      apiExtension.contributeAdminApiRoutes(routeBuilder);
    }
  }

  /**
   * Finds the {@link AdminTask} that corresponds to the given request method and path.
   *
   * <p>If no specific task is mapped to the request, a {@link NotFoundAdminTask} is returned.
   *
   * @param method The HTTP {@link RequestMethod} of the request.
   * @param path The URL path of the request.
   * @return The matching {@link AdminTask}, or a {@link NotFoundAdminTask} if no match is found.
   */
  public AdminTask taskFor(final RequestMethod method, final String path) {
    return routes.entrySet().stream()
        .filter(entry -> entry.getKey().matches(method, path))
        .map(Entry::getValue)
        .findFirst()
        .orElseGet(NotFoundAdminTask::new);
  }

  /**
   * Finds the {@link RequestSpec} for a given {@link AdminTask} class.
   *
   * <p>This performs a reverse lookup to determine the URL and HTTP method that trigger a specific
   * task. It is useful for clients that need to construct URLs for admin operations.
   *
   * @param taskClass The class of the {@link AdminTask} to find the route for.
   * @return The corresponding {@link RequestSpec}.
   * @throws NotFoundException if no route is registered for the specified task class.
   */
  public RequestSpec requestSpecForTask(final Class<? extends AdminTask> taskClass) {
    return routes.entrySet().stream()
        .filter(input -> input.getValue().getClass().equals(taskClass))
        .map(Entry::getKey)
        .findFirst()
        .orElseThrow(
            () ->
                new NotFoundException("No route could be found for " + taskClass.getSimpleName()));
  }

  /**
   * A builder for creating the immutable map of routes.
   *
   * <p>This class implements the {@link Router} interface to provide a simple API for adding new
   * routes during the initialization phase.
   */
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
