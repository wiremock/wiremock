package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

public class FindStubMappingsByMetadataTask implements AdminTask {

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        StringValuePattern pattern = Json.read(request.getBodyAsString(), StringValuePattern.class);
        ListStubMappingsResult stubMappings = admin.findAllStubsByMetadata(pattern);
        return ResponseDefinition.okForJson(stubMappings);
    }
}
