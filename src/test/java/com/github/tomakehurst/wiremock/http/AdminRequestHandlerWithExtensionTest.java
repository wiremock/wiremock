package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class AdminRequestHandlerWithExtensionTest {
    public static class TaskDecorator implements AdminTask {
        final AdminTask decoratedTask;

        public TaskDecorator(AdminTask decoratedTask) {
            this.decoratedTask = decoratedTask;
        }

        @Override
        public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
            // some additional behavior
            return decoratedTask.execute(admin, request, pathParams);
        }
    }

    public static class TaskA implements AdminTask {

        @Override
        public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
            throw new AssertionError("Mustn't try to parse RequestSpec for this task, when asking for TaskB execution");
        }
    }

    public static class TaskB implements AdminTask {
        boolean wasExecuted;

        @Override
        public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
            System.out.println("Executing task B, must be here when asking for TaskB execution");
            wasExecuted = true;
            return null;
        }
    }

    public static class TestExtension implements AdminApiExtension {
        TaskB taskB = new TaskB();

        @Override
        public void contributeAdminApiRoutes(Router router) {
            router.add(RequestMethod.GET, "/execute/TaskA/please", new TaskDecorator(new TaskA()));
            router.add(RequestMethod.GET, "/execute/TaskB/please", new TaskDecorator(taskB));
        }

        @Override
        public String getName() {
            return "TestExtension";
        }
    }

    private Mockery context;
    private TestExtension testExtension;
    private AdminRequestHandler handler;

    @Before
    public void setUp() {
        testExtension = new TestExtension();
        context = new Mockery();
        Admin admin = context.mock(Admin.class);
        AdminRoutes adminRoutes = AdminRoutes.defaultsPlus(asList((AdminApiExtension) testExtension), context.mock(AdminTask.class));

        handler = new AdminRequestHandler(adminRoutes, admin, new BasicResponseRenderer(), new NoAuthenticator(), false, Collections.<RequestFilter>emptyList());
    }

    @Test
    public void handleRequestShouldFindRightTaskAccordingRequestSpec() {
        Request requestForTaskB = aRequest(context)
                .withUrl("/execute/TaskB/please")
                .withMethod(GET)
                .build();

        handler.handleRequest(requestForTaskB);

        assertTrue(testExtension.taskB.wasExecuted);
    }
}