package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SingleItemResult;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.UUID;

public class GetStubMappingTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        String idString = pathParams.get("id");
        UUID id = UUID.fromString(idString);

        SingleItemResult<StubMapping> stubMappingResult = admin.getStubMapping(id);
        return stubMappingResult.isPresent() ?
            ResponseDefinition.okForJson(stubMappingResult.getItem()) :
            ResponseDefinition.notFound();
    }
}
