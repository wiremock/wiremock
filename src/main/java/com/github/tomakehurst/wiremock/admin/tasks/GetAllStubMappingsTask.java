package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.LimitAndOffsetPaginator;
import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class GetAllStubMappingsTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        ListStubMappingsResult result = new ListStubMappingsResult(
            LimitAndOffsetPaginator.fromRequest(admin.listAllStubMappings().getMappings(), request)
        );

        return ResponseDefinitionBuilder.jsonResponse(result);
    }
}
