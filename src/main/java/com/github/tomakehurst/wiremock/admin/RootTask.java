package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;

public class RootTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        ListStubMappingsResult result = admin.listAllStubMappings();
        return ResponseDefinitionBuilder.jsonResponse(result);
    }
}
