package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class SaveMappingsTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        admin.saveMappings();
        return ResponseDefinition.ok();
    }
}
