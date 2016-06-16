package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
public class RemoveStubMappingTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        StubMapping removeMapping = StubMapping.buildFrom(request.getBodyAsString());
        admin.removeStubMapping(removeMapping);
        return ResponseDefinition.ok();
    }
}
