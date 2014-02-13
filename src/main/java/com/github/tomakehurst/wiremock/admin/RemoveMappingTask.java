package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 07/02/14
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveMappingTask implements AdminTask{

    @Override
    public ResponseDefinition execute(Admin admin, Request request) {
        HashMap<String, Object> reqBody = Json.read(request.getBodyAsString(), HashMap.class);
        if(admin.removeMapping(new Long((Integer)reqBody.get("id")))){
            return ResponseDefinition.ok();
        }
        return ResponseDefinition.notFound();
    }
}
