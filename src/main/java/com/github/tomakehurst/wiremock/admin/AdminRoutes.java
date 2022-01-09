/*
 * Copyright (C) 2016-2021 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.verification.notmatched.PlainTextStubNotMatchedRenderer;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.Map;

public class AdminRoutes {

  private final ImmutableBiMap<RequestSpec, AdminTask> routes;
  private final Iterable<AdminApiExtension> apiExtensions;

  public static AdminRoutes defaults() {
    return new AdminRoutes(
        Collections.<AdminApiExtension>emptyList(), new PlainTextStubNotMatchedRenderer());
  }

  public static AdminRoutes defaultsPlus(
      final Iterable<AdminApiExtension> apiExtensions, final AdminTask notMatchedTask) {
        return new AdminRoutes(apiExtensions, notMatchedTask);
    }

  protected AdminRoutes(final Iterable<AdminApiExtension> apiExtensions, final AdminTask notMatchedTask) {
        this.apiExtensions = apiExtensions;
        final RouteBuilder routeBuilder = new RouteBuilder();
        this.initDefaultRoutes(routeBuilder);
    this.initAdditionalRoutes(routeBuilder);
    routeBuilder.add(ANY, "/not-matched", notMatchedTask);
    this.routes = routeBuilder.build();
  }

  private void initDefaultRoutes(final Router router) {
    router.add(GET, "/", RootTask.class);
    router.add(GET, "", RootRedirectTask.class);
    router.add(POST, "/reset", ResetTask.class);

    router.add(GET, "/mappings", GetAllStubMappingsTask.class);
    router.add(POST, "/mappings", CreateStubMappingTask.class);
    router.add(DELETE, "/mappings", ResetStubMappingsTask.class);

    router.add(POST, "/mappings/new", OldCreateStubMappingTask.class); // Deprecated
    router.add(POST, "/mappings/remove", OldRemoveStubMappingTask.class); // Deprecated
    router.add(POST, "/mappings/edit", OldEditStubMappingTask.class); // Deprecated
    router.add(POST, "/mappings/save", SaveMappingsTask.class);
    router.add(POST, "/mappings/reset", ResetToDefaultMappingsTask.class);
    router.add(GET, "/mappings/{id}", GetStubMappingTask.class);
    router.add(PUT, "/mappings/{id}", EditStubMappingTask.class);
    router.add(DELETE, "/mappings/{id}", RemoveStubMappingTask.class);
    router.add(POST, "/mappings/find-by-metadata", FindStubMappingsByMetadataTask.class);
    router.add(POST, "/mappings/remove-by-metadata", RemoveStubMappingsByMetadataTask.class);
    router.add(POST, "/mappings/import", ImportStubMappingsTask.class);

    router.add(GET, "/files", GetAllStubFilesTask.class);
    router.add(GET, "/files/{filename}", GetStubFilesTask.class);
        router.add(PUT, "/files/**", EditStubFileTask.class);
    router.add(DELETE, "/files/**", DeleteStubFileTask.class);

    router.add(GET, "/scenarios", GetAllScenariosTask.class);
    router.add(POST, "/scenarios/reset", ResetScenariosTask.class);

    router.add(GET, "/requests", GetAllRequestsTask.class);
    router.add(DELETE, "/requests", ResetRequestsTask.class);
    router.add(POST, "/requests/reset", OldResetRequestsTask.class); // Deprecated
    router.add(POST, "/requests/count", GetRequestCountTask.class);
    router.add(POST, "/requests/find", FindRequestsTask.class);
    router.add(GET, "/requests/unmatched", FindUnmatchedRequestsTask.class);
    router.add(GET, "/requests/unmatched/near-misses", FindNearMissesForUnmatchedTask.class);
    router.add(GET, "/requests/{id}", GetServedStubTask.class);
    router.add(DELETE, "/requests/{id}", RemoveServeEventTask.class);
    router.add(POST, "/requests/remove", RemoveServeEventsByRequestPatternTask.class);
    router.add(POST, "/requests/remove-by-metadata", RemoveServeEventsByStubMetadataTask.class);

    router.add(POST, "/recordings/snapshot", SnapshotTask.class);
    router.add(POST, "/recordings/start", StartRecordingTask.class);
    router.add(POST, "/recordings/stop", StopRecordingTask.class);
    router.add(GET, "/recordings/status", GetRecordingStatusTask.class);
    router.add(GET, "/recorder", GetRecordingsIndexTask.class);

    router.add(POST, "/near-misses/request", FindNearMissesForRequestTask.class);
    router.add(POST, "/near-misses/request-pattern", FindNearMissesForRequestPatternTask.class);

    router.add(GET, "/settings", GetGlobalSettingsTask.class);
    router.add(PUT, "/settings", GlobalSettingsUpdateTask.class);
    router.add(POST, "/settings", GlobalSettingsUpdateTask.class);
    router.add(PATCH, "/settings/extended", PatchExtendedSettingsTask.class);

    router.add(POST, "/shutdown", ShutdownServerTask.class);

    router.add(GET, "/docs/swagger", GetSwaggerSpecTask.class);
    router.add(GET, "/docs", GetDocIndexTask.class);

        router.add(GET, "/certs/wiremock-ca.crt", GetCaCertTask.class);

        router.add(GET, "/proxy", GetProxyConfigTask.class);
        router.add(PUT, "/proxy/{id}", EnableProxyTask.class);
        router.add(DELETE, "/proxy/{id}", DisableProxyTask.class);
    }

  protected void initAdditionalRoutes(final Router routeBuilder) {
    for (final AdminApiExtension apiExtension : this.apiExtensions) {
      apiExtension.contributeAdminApiRoutes(routeBuilder);
    }
  }

  public AdminTask taskFor(final RequestMethod method, final String path) {
    return tryFind(
            this.routes.entrySet(),
            new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
              @Override
              public boolean apply(final Map.Entry<RequestSpec, AdminTask> entry) {
                return entry.getKey().matches(method, path);
              }
            })
        .transform(
            new Function<Map.Entry<RequestSpec, AdminTask>, AdminTask>() {
              @Override
              public AdminTask apply(final Map.Entry<RequestSpec, AdminTask> input) {
                return input.getValue();
              }
            })
        .or(new NotFoundAdminTask());
  }

  public RequestSpec requestSpecForTask(final Class<? extends AdminTask> taskClass) {
    final RequestSpec requestSpec = tryFind(this.routes.entrySet(),
                new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
                  @Override
                  public boolean apply(final Map.Entry<RequestSpec, AdminTask> input) {
                return input.getValue().getClass().equals(taskClass);}
                })
            .transform(new Function<Map.Entry<RequestSpec, AdminTask>, RequestSpec>() {
            @Override
            public RequestSpec apply(final Map.Entry<RequestSpec, AdminTask> input) {
                return input.getKey();}
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
      this.builder = ImmutableBiMap.builder();
    }

    @Override
    public void add(
        final RequestMethod method, final String urlTemplate, final Class<? extends AdminTask> taskClass) {
            try {
                final AdminTask task = taskClass.getDeclaredConstructor().newInstance();
        this.add(requestSpec(method, urlTemplate), task);
      } catch (final Exception e) {
                throwUnchecked(e);
            }
        }

    @Override
    public void add(final RequestMethod method, final String urlTemplate, final AdminTask task) {
            this.add(requestSpec(method, urlTemplate), task);
    }

    public void add(final RequestSpec requestSpec, final AdminTask task) {
            this.builder.put(requestSpec, task);
    }

    ImmutableBiMap<RequestSpec, AdminTask> build() {
      return this.builder.build();
    }
  }
}
