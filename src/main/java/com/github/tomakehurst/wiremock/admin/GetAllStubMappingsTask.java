package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class GetAllStubMappingsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        ListStubMappingsResult result = new ListStubMappingsResult(
            LimitAndOffsetPaginator.fromRequest(admin.listAllStubMappings().getMappings(), request)
        );

        return ResponseDefinitionBuilder.jsonResponse(result);
    }
}
