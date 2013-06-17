package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import static com.github.tomakehurst.wiremock.admin.RequestSpec.requestSpec;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

public class AdminTasks {

    private static final BiMap<RequestSpec, Class<? extends AdminTask>> TASKS =
            new ImmutableBiMap.Builder<RequestSpec, Class<? extends AdminTask>>()
                .put(requestSpec(GET, "/"), RootTask.class)
                .put(requestSpec(GET, ""), RootRedirectTask.class)
                .put(requestSpec(POST, "/reset"), ResetTask.class)
                .put(requestSpec(POST, "/mappings/new"), NewStubMappingTask.class)
                .put(requestSpec(POST, "/scenarios/reset"), ResetScenariosTask.class)
                .put(requestSpec(POST, "/mappings/reset"), ResetToDefaultMappingsTask.class)
                .put(requestSpec(POST, "/requests/count"), GetRequestCountTask.class)
                .put(requestSpec(POST, "/requests/find"), FindRequestsTask.class)
                .put(requestSpec(POST, "/socket-delay"), SocketDelayTask.class)
                .put(requestSpec(POST, "/settings"), GlobalSettingsUpdateTask.class)
                .build();

    public static AdminTask taskFor(RequestMethod method, String path) {
        Class<? extends AdminTask> taskClass = TASKS.get(requestSpec(method, path));
        if (taskClass == null) {
            return new NotFoundAdminTask();
        }

        try {
            return taskClass.newInstance();
        } catch (Exception e) {
            return throwUnchecked(e, AdminTask.class);
        }
    }

    public static RequestSpec requestSpecForTask(Class<? extends AdminTask> taskClass) {
        return TASKS.inverse().get(taskClass);
    }
}
