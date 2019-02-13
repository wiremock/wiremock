package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.StubImport;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class ImportStubMappingsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        StubImport stubImport = Json.read(request.getBodyAsString(), StubImport.class);
        admin.importStubs(stubImport);
        return ResponseDefinition.ok();
    }
}
