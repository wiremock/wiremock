package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class ResetStubMappingsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        admin.resetMappings();
        return ResponseDefinition.okEmptyJson();
    }
}
