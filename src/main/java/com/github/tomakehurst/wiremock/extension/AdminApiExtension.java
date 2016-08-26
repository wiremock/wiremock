package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.admin.Router;

public interface AdminApiExtension extends Extension {

    /**
     * To be overridden if the extension needs to expose new API resources under /__admin
     * @param router the admin route builder
     */
    void contributeAdminApiRoutes(Router router);
}
