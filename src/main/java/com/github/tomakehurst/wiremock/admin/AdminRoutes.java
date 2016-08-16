package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.collect.ImmutableBiMap;

import static com.github.tomakehurst.wiremock.admin.RequestSpec.requestSpec;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

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

        router.add(POST, "/scenarios/reset", ResetScenariosTask.class);  // Deprecated

        router.add(POST, "/requests/reset", ResetRequestsTask.class);  // Deprecated
        router.add(POST, "/requests/count", GetRequestCountTask.class);
        router.add(POST, "/requests/find", FindRequestsTask.class);
        router.add(GET,  "/requests/unmatched", FindUnmatchedRequestsTask.class);
        router.add(GET,  "/requests/unmatched/near-misses", FindNearMissesForUnmatchedTask.class);

        router.add(POST, "/near-misses/request", FindNearMissesForRequestTask.class);
        router.add(POST, "/near-misses/request-pattern", FindNearMissesForRequestPatternTask.class);

        router.add(POST, "/settings", GlobalSettingsUpdateTask.class);
        router.add(POST, "/shutdown", ShutdownServerTask.class);
    }

    protected void initAdditionalRoutes(Router router) {
    }

    public AdminTask taskFor(RequestMethod method, String path) {
        Class<? extends AdminTask> taskClass = routes.get(requestSpec(method, path));
        if (taskClass == null) {
            return new NotFoundAdminTask();
        }

        try {
            return taskClass.newInstance();
        } catch (Exception e) {
            return throwUnchecked(e, AdminTask.class);
        }
    }

    public RequestSpec requestSpecForTask(Class<? extends AdminTask> taskClass) {
        return routes.inverse().get(taskClass);
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
