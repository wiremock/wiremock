package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

/**
 * @author Christopher Holomek
 */
public class GetProxyConfigTask implements AdminTask {
    @Override
    public ResponseDefinition execute(final Admin admin, final Request request, final PathParams pathParams) {
        return ResponseDefinition.okForJson(admin.getProxyConfig());
    }
}
