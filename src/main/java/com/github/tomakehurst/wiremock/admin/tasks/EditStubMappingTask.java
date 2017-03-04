package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.UUID;

public class EditStubMappingTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        StubMapping newStubMapping = StubMapping.buildFrom(request.getBodyAsString());
        UUID id = UUID.fromString(pathParams.get("id"));
        SingleStubMappingResult stubMappingResult = admin.getStubMapping(id);
        if (!stubMappingResult.isPresent()) {
            return ResponseDefinition.notFound();
        }

        newStubMapping.setId(id);

        admin.editStubMapping(newStubMapping);
        return ResponseDefinition.okForJson(newStubMapping);
    }
}
