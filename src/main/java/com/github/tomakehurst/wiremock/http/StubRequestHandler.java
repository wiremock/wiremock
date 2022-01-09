/*
 * Copyright (C) 2011-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.PostServeActionDefinition;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.jetty9.websockets.Message;
import com.github.tomakehurst.wiremock.jetty9.websockets.WebSocketEndpoint;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import java.util.List;
import java.util.Map;

public class StubRequestHandler extends AbstractRequestHandler {

    private final StubServer stubServer;
    private final Admin admin;
    private final Map<String, PostServeAction> postServeActions;
    private final RequestJournal requestJournal;
    private final boolean loggingDisabled;

  public StubRequestHandler(
      final StubServer stubServer,
                              final ResponseRenderer responseRenderer,
                              final Admin admin,
                              final Map<String, PostServeAction> postServeActions,
                              final RequestJournal requestJournal,
                              final List<RequestFilter> requestFilters,
                              boolean loggingDisabled) {
		super(responseRenderer, requestFilters);
		this.stubServer = stubServer;
        this.admin = admin;
        this.postServeActions = postServeActions;
        this.requestJournal = requestJournal;
        this.loggingDisabled = loggingDisabled;
    }

    @Override
    public ServeEvent handleRequest(final Request request) {
        return this.stubServer.serveStubFor(request);
    }

    @Override
    protected boolean logRequests() {
        return !loggingDisabled;
    }

  @Override
  protected void beforeResponseSent(final ServeEvent serveEvent, final Response response) {
        this.requestJournal.requestReceived(serveEvent);

        if (serveEvent.getWasMatched()) {
            WebSocketEndpoint.broadcast(Message.MATCHED);
        } else {
            WebSocketEndpoint.broadcast(Message.UNMATCHED);
        }
    }

  @Override
  protected void afterResponseSent(final ServeEvent serveEvent, final Response response) {
    for (final PostServeAction postServeAction : this.postServeActions.values()) {
      postServeAction.doGlobalAction(serveEvent, this.admin);
    }

    List<PostServeActionDefinition> postServeActionDefs = serveEvent.getPostServeActions();
    for (PostServeActionDefinition postServeActionDef : postServeActionDefs) {
      PostServeAction action = postServeActions.get(postServeActionDef.getName());
      if (action != null) {
        Parameters parameters = postServeActionDef.getParameters();
        action.doAction(serveEvent, admin, parameters);
      } else {
        notifier().error("No extension was found named \"" + postServeActionDef.getName() + "\"");
      }
    }
  }
}
