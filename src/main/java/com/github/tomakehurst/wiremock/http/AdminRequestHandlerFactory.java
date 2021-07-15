package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.admin.AdminRoutes;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;

import java.util.List;

public class AdminRequestHandlerFactory {

    public AdminRequestHandler buildAdminRequestHandler(Admin admin,
                                                        Options options, List<RequestFilter> adminRequestFilters) {
        AdminRoutes adminRoutes = AdminRoutes.defaultsPlus(
                options.extensionsOfType(AdminApiExtension.class).values(),
                options.getNotMatchedRenderer()
        );
        return new AdminRequestHandler(
                adminRoutes,
                admin,
                new BasicResponseRenderer(),
                options.getAdminAuthenticator(),
                options.getHttpsRequiredForAdminApi(),
                adminRequestFilters
        );
    }
}
