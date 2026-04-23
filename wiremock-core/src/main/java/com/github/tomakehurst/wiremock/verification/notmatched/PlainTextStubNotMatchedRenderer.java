/*
 * Copyright (C) 2017-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.verification.notmatched;

import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import com.github.tomakehurst.wiremock.verification.diff.Diff;
import com.github.tomakehurst.wiremock.verification.diff.PlainTextDiffRenderer;
import java.util.List;
import java.util.Map;

public class PlainTextStubNotMatchedRenderer extends NotMatchedRenderer {

  public static final String CONSOLE_WIDTH_HEADER_KEY = "X-WireMock-Console-Width";

  private final Extensions extensions;

  public PlainTextStubNotMatchedRenderer(Extensions extensions) {
    this.extensions = extensions;
  }

  @Override
  public ResponseDefinition render(Admin admin, ServeEvent serveEvent) {
    LoggedRequest loggedRequest = serveEvent.getRequest();
    List<NearMiss> nearMisses = admin.findTopNearMissesFor(loggedRequest).getNearMisses();

    Map<String, RequestMatcherExtension> customMatcherExtensions =
        extensions.ofType(RequestMatcherExtension.class);

    PlainTextDiffRenderer diffRenderer =
        loggedRequest.containsHeader(CONSOLE_WIDTH_HEADER_KEY)
            ? new PlainTextDiffRenderer(
                customMatcherExtensions,
                Integer.parseInt(loggedRequest.getHeader(CONSOLE_WIDTH_HEADER_KEY)))
            : new PlainTextDiffRenderer(customMatcherExtensions);

    String body;
    if (nearMisses.isEmpty()) {
      body = "No response could be served as there are no stub mappings in this WireMock instance.";
    } else {
      Diff firstDiff = nearMisses.get(0).getDiff();
      body = diffRenderer.render(firstDiff);
    }

    notifier().error(body);

    return ResponseDefinitionBuilder.responseDefinition()
        .withStatus(404)
        .withHeader(CONTENT_TYPE, "text/plain")
        .withBody(body)
        .build();
  }
}
