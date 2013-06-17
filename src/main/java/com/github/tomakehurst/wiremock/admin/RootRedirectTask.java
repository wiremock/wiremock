package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;

public class RootRedirectTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        return ResponseDefinition.redirectTo(ADMIN_CONTEXT_ROOT + "/");
    }
}
