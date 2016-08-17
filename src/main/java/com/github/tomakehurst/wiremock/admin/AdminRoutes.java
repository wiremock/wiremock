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
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;

import java.util.Map;

import static com.github.tomakehurst.wiremock.admin.RequestSpec.requestSpec;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.google.common.collect.Iterables.tryFind;

public class AdminRoutes {

    private final ImmutableBiMap<RequestSpec, Class<? extends AdminTask>> routes;

    public static AdminRoutes defaults() {
        return new AdminRoutes();
    }

    protected AdminRoutes() {
        Router router = new Router();
        initDefaultRoutes(router);
        initAdditionalRoutes(router);
        routes = router.build();
    }

    private void initDefaultRoutes(Router router) {
        router.add(GET,  "/", RootTask.class);
        router.add(GET,  "", RootRedirectTask.class);
        router.add(POST, "/reset", ResetTask.class);

        router.add(GET,  "/mappings", GetAllStubMappingsTask.class);
        router.add(POST, "/mappings", CreateStubMappingTask.class);

        router.add(POST, "/mappings/new", StubMappingTask.class); // Deprecated
        router.add(POST, "/mappings/remove", RemoveStubMappingTask.class);  // Deprecated
        router.add(POST, "/mappings/edit", EditStubMappingTask.class);  // Deprecated
        router.add(POST, "/mappings/save", SaveMappingsTask.class);
        router.add(POST, "/mappings/reset", ResetToDefaultMappingsTask.class);  // Deprecated
        router.add(GET,  "/mappings/{id}", GetStubMappingTask.class);

        router.add(POST, "/scenarios/reset", ResetScenariosTask.class);  // Deprecated

        router.add(GET,  "/requests", GetAllRequestsTask.class);
        router.add(POST, "/requests/reset", ResetRequestsTask.class);  // Deprecated
        router.add(POST, "/requests/count", GetRequestCountTask.class);
        router.add(POST, "/requests/find", FindRequestsTask.class);
        router.add(GET,  "/requests/unmatched", FindUnmatchedRequestsTask.class);
        router.add(GET,  "/requests/unmatched/near-misses", FindNearMissesForUnmatchedTask.class);
        router.add(GET,  "/requests/{id}", GetServedStubTask.class);


        router.add(POST, "/near-misses/request", FindNearMissesForRequestTask.class);
        router.add(POST, "/near-misses/request-pattern", FindNearMissesForRequestPatternTask.class);

        router.add(POST, "/settings", GlobalSettingsUpdateTask.class);
        router.add(POST, "/shutdown", ShutdownServerTask.class);
    }

    protected void initAdditionalRoutes(Router router) {
    }

    public AdminTask taskFor(final RequestMethod method, final String path) {
        Class<? extends AdminTask> taskClass = tryFind(routes.entrySet(), new Predicate<Map.Entry<RequestSpec, Class<? extends AdminTask>>>() {
            @Override
            public boolean apply(Map.Entry<RequestSpec, Class<? extends AdminTask>> entry) {
                return entry.getKey().matches(method, path);
            }
        }).transform(new Function<Map.Entry<RequestSpec,Class<? extends AdminTask>>, Class<? extends AdminTask>>() {
            @Override
            public Class<? extends AdminTask> apply(Map.Entry<RequestSpec, Class<? extends AdminTask>> input) {
                return input.getValue();
            }
        }).or(NotFoundAdminTask.class);

        try {
            return taskClass.newInstance();
        } catch (Exception e) {
            return throwUnchecked(e, AdminTask.class);
        }
    }

    public RequestSpec requestSpecForTask(Class<? extends AdminTask> taskClass) {
        RequestSpec requestSpec = routes.inverse().get(taskClass);
        if (requestSpec == null) {
            throw new NotFoundException("No route could be found for " + taskClass.getSimpleName());
        }
        return requestSpec;
    }

    protected static class Router {
        private final ImmutableBiMap.Builder<RequestSpec, Class<? extends AdminTask>> builder;

        public Router() {
            builder = ImmutableBiMap.builder();
        }

        public void add(RequestMethod method, String urlTemplate, Class<? extends AdminTask> task) {
            add(requestSpec(method, urlTemplate), task);
        }

        public void add(RequestSpec requestSpec, Class<? extends AdminTask> task) {
            builder.put(requestSpec, task);
        }

        ImmutableBiMap<RequestSpec, Class<? extends AdminTask>> build() {
            return builder.build();
        }
    }
}
