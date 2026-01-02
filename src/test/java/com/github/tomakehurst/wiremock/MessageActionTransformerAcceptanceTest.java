/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.extension.MessageActionTransformer;
import com.github.tomakehurst.wiremock.message.MessageAction;
import com.github.tomakehurst.wiremock.message.MessageActionContext;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MessageActionTransformerAcceptanceTest {

  WireMockServer wm;

  @AfterEach
  void cleanup() {
    if (wm != null) {
      wm.stop();
    }
  }

  @Test
  void globalTransformerModifiesMessageAction() {
    wm =
        new WireMockServer(
                wireMockConfig().dynamicPort().extensions(new PrefixingMessageActionTransformer())).startServer();

    wm.addMessageStubMapping(
        message()
            .withName("Transformed stub")
            .withBody(equalTo("hello"))
            .willTriggerActions(sendMessage("world").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/transform-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "hello");
    assertThat(response, is("[TRANSFORMED] world"));
  }

  @Test
  void multipleTransformersAreAppliedInOrder() {
    wm =
        new WireMockServer(
                wireMockConfig()
                    .dynamicPort()
                    .extensions(
                        new PrefixingMessageActionTransformer(),
                        new SuffixingMessageActionTransformer()))
            .startServer();

    wm.addMessageStubMapping(
        message()
            .withName("Multi-transform stub")
            .withBody(equalTo("test"))
            .willTriggerActions(sendMessage("message").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/multi-transform-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response, is("[TRANSFORMED] message [END]"));
  }

  @Test
  void nonGlobalTransformerIsNotApplied() {
    wm =
        new WireMockServer(
                wireMockConfig().dynamicPort().extensions(new NonGlobalMessageActionTransformer()))
            .startServer();

    wm.addMessageStubMapping(
        message()
            .withName("Non-global stub")
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage("pong").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/non-global-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "ping");
    assertThat(response, is("pong"));
  }

  @Test
  void transformerHasAccessToIncomingMessageContext() {
    wm =
        new WireMockServer(
                wireMockConfig().dynamicPort().extensions(new EchoingMessageActionTransformer()))
            .startServer();

    wm.addMessageStubMapping(
        message()
            .withName("Echo context stub")
            .withBody(matching(".*"))
            .willTriggerActions(sendMessage("response").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/echo-context-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "my-input");
    assertThat(response, is("Echo: my-input"));
  }

  @Test
  void nonGlobalTransformerIsAppliedWhenSpecifiedOnAction() {
    wm =
        new WireMockServer(
                wireMockConfig().dynamicPort().extensions(new NonGlobalMessageActionTransformer()))
            .startServer();

    wm.addMessageStubMapping(
        message()
            .withName("Selective transformer stub")
            .withBody(equalTo("apply"))
            .willTriggerActions(
                sendMessage("original").withTransformer("non-global").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/selective-transform-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "apply");
    assertThat(response, is("SHOULD NOT SEE THIS"));
  }

  private String websocketUrl(String path) {
    return "ws://localhost:" + wm.port() + path;
  }

  public static class PrefixingMessageActionTransformer implements MessageActionTransformer {
    @Override
    public MessageAction transform(MessageAction action, MessageActionContext context) {
      if (action instanceof SendMessageAction sendAction) {
        String originalBody = getMessageBody(sendAction);
        return SendMessageAction.toOriginatingChannel("[TRANSFORMED] " + originalBody);
      }
      return action;
    }

    @Override
    public String getName() {
      return "prefixing";
    }
  }

  public static class SuffixingMessageActionTransformer implements MessageActionTransformer {
    @Override
    public MessageAction transform(MessageAction action, MessageActionContext context) {
      if (action instanceof SendMessageAction sendAction) {
        String originalBody = getMessageBody(sendAction);
        return SendMessageAction.toOriginatingChannel(originalBody + " [END]");
      }
      return action;
    }

    @Override
    public String getName() {
      return "suffixing";
    }
  }

  public static class NonGlobalMessageActionTransformer implements MessageActionTransformer {
    @Override
    public MessageAction transform(MessageAction action, MessageActionContext context) {
      if (action instanceof SendMessageAction sendAction) {
        return SendMessageAction.toOriginatingChannel("SHOULD NOT SEE THIS");
      }
      return action;
    }

    @Override
    public boolean applyGlobally() {
      return false;
    }

    @Override
    public String getName() {
      return "non-global";
    }
  }

  public static class EchoingMessageActionTransformer implements MessageActionTransformer {
    @Override
    public MessageAction transform(MessageAction action, MessageActionContext context) {
      if (action instanceof SendMessageAction && context.isTriggeredByMessage()) {
        String incomingBody = context.getIncomingMessage().getBodyAsString();
        return SendMessageAction.toOriginatingChannel("Echo: " + incomingBody);
      }
      return action;
    }

    @Override
    public String getName() {
      return "echoing";
    }
  }

  private static String getMessageBody(SendMessageAction action) {
    EntityDefinition body = action.getBody();
    if (body instanceof StringEntityDefinition stringDef) {
      return stringDef.getValue();
    }
    if (body instanceof TextEntityDefinition textDef) {
      Object data = textDef.getData();
      return data != null ? data.toString() : "";
    }
    return "";
  }
}
