package com.github.tomakehurst.wiremock.verification.notmatched;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import com.github.tomakehurst.wiremock.verification.diff.Diff;
import com.github.tomakehurst.wiremock.verification.diff.PlainTextDiffRenderer;

import java.util.List;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

public class PlainTextStubNotMatchedRenderer extends NotMatchedRenderer {

    public static final String CONSOLE_WIDTH_HEADER_KEY = "X-WireMock-Console-Width";

    @Override
    public ResponseDefinition render(Admin admin, Request request) {
        LoggedRequest loggedRequest = LoggedRequest.createFrom(request.getOriginalRequest().or(request));
        List<NearMiss> nearMisses = admin.findTopNearMissesFor(loggedRequest).getNearMisses();

        PlainTextDiffRenderer diffRenderer = loggedRequest.containsHeader(CONSOLE_WIDTH_HEADER_KEY) ?
            new PlainTextDiffRenderer(Integer.parseInt(loggedRequest.getHeader(CONSOLE_WIDTH_HEADER_KEY))) :
            new PlainTextDiffRenderer();

        String body;
        if (nearMisses.isEmpty()) {
            body = "No response could be served as there are no stub mappings in this WireMock instance.";
        } else {
            Diff firstDiff = nearMisses.get(0).getDiff();
            body = diffRenderer.render(firstDiff);
        }

        return ResponseDefinitionBuilder.responseDefinition()
            .withStatus(404)
            .withHeader(CONTENT_TYPE, "text/plain")
            .withBody(body)
            .build();
    }
}
