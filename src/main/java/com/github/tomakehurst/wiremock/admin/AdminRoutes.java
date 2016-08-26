/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.admin.RequestSpec.requestSpec;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.google.common.collect.Iterables.tryFind;

public class AdminRoutes {

    private final ImmutableBiMap<RequestSpec, AdminTask> routes;
    private final Iterable<AdminApiExtension> apiExtensions;

    public static AdminRoutes defaults() {
        return new AdminRoutes(Collections.<AdminApiExtension>emptyList());
    }

    public static AdminRoutes defaultsPlus(Iterable<AdminApiExtension> apiExtensions) {
        return new AdminRoutes(apiExtensions);
    }

    protected AdminRoutes(Iterable<AdminApiExtension> apiExtensions) {
        this.apiExtensions = apiExtensions;
        RouteBuilder routeBuilder = new RouteBuilder();
        initDefaultRoutes(routeBuilder);
        initAdditionalRoutes(routeBuilder);
        routes = routeBuilder.build();
    }

    private void initDefaultRoutes(Router routeBuilder) {
        routeBuilder.add(GET,  "/", RootTask.class);
        routeBuilder.add(GET,  "", RootRedirectTask.class);
        routeBuilder.add(POST, "/reset", ResetTask.class);

        routeBuilder.add(GET,  "/mappings", GetAllStubMappingsTask.class);
        routeBuilder.add(POST, "/mappings", CreateStubMappingTask.class);

        routeBuilder.add(POST, "/mappings/new", StubMappingTask.class); // Deprecated
        routeBuilder.add(POST, "/mappings/remove", RemoveStubMappingTask.class);  // Deprecated
        routeBuilder.add(POST, "/mappings/edit", EditStubMappingTask.class);  // Deprecated
        routeBuilder.add(POST, "/mappings/save", SaveMappingsTask.class);
        routeBuilder.add(POST, "/mappings/reset", ResetToDefaultMappingsTask.class);  // Deprecated
        routeBuilder.add(GET,  "/mappings/{id}", GetStubMappingTask.class);

        routeBuilder.add(POST, "/scenarios/reset", ResetScenariosTask.class);  // Deprecated

        routeBuilder.add(GET,  "/requests", GetAllRequestsTask.class);
        routeBuilder.add(POST, "/requests/reset", ResetRequestsTask.class);  // Deprecated
        routeBuilder.add(POST, "/requests/count", GetRequestCountTask.class);
        routeBuilder.add(POST, "/requests/find", FindRequestsTask.class);
        routeBuilder.add(GET,  "/requests/unmatched", FindUnmatchedRequestsTask.class);
        routeBuilder.add(GET,  "/requests/unmatched/near-misses", FindNearMissesForUnmatchedTask.class);
        routeBuilder.add(GET,  "/requests/{id}", GetServedStubTask.class);


        routeBuilder.add(POST, "/near-misses/request", FindNearMissesForRequestTask.class);
        routeBuilder.add(POST, "/near-misses/request-pattern", FindNearMissesForRequestPatternTask.class);

        routeBuilder.add(POST, "/settings", GlobalSettingsUpdateTask.class);
        routeBuilder.add(POST, "/shutdown", ShutdownServerTask.class);
    }

    protected void initAdditionalRoutes(Router routeBuilder) {
        for (AdminApiExtension apiExtension: apiExtensions) {
            apiExtension.contributeAdminApiRoutes(routeBuilder);
        }
    }

    public AdminTask taskFor(final RequestMethod method, final String path) {
        return tryFind(routes.entrySet(), new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
            @Override
            public boolean apply(Map.Entry<RequestSpec, AdminTask> entry) {
                return entry.getKey().matches(method, path);
            }
        }).transform(new Function<Map.Entry<RequestSpec, AdminTask>, AdminTask>() {
            @Override
            public AdminTask apply(Map.Entry<RequestSpec, AdminTask> input) {
                return input.getValue();
            }
        }).or(new NotFoundAdminTask());
    }

    public RequestSpec requestSpecForTask(final Class<? extends AdminTask> taskClass) {
        RequestSpec requestSpec = tryFind(routes.entrySet(), new Predicate<Map.Entry<RequestSpec, AdminTask>>() {
            @Override
            public boolean apply(Map.Entry<RequestSpec, AdminTask> input) {
                return input.getValue().getClass().equals(taskClass);
            }
        }).transform(new Function<Map.Entry<RequestSpec,AdminTask>, RequestSpec>() {
            @Override
            public RequestSpec apply(Map.Entry<RequestSpec, AdminTask> input) {
                return input.getKey();
            }
        }).orNull();

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
        public void add(RequestMethod method, String urlTemplate, Class<? extends AdminTask> taskClass) {
            try {
                AdminTask task = taskClass.newInstance();
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
