package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.util.UUID;

/**
 * @author Christopher Holomek
 */
public class EnableProxyTask implements AdminTask {
    @Override
    public ResponseDefinition execute(final Admin admin, final Request request, final PathParams pathParams) {
        final String idString = pathParams.get("id");
        final UUID id = UUID.fromString(idString);
        admin.enableProxy(id);
        return ResponseDefinition.ok();
    }
}
